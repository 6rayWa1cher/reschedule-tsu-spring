package com.a6raywa1cher.rescheduletsuspring.components.tsudbimporter;

import com.a6raywa1cher.rescheduletsuspring.externalmodels.*;
import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import com.a6raywa1cher.rescheduletsuspring.models.WeekSign;
import com.a6raywa1cher.rescheduletsuspring.models.submodels.LessonCellCoordinates;
import com.a6raywa1cher.rescheduletsuspring.service.interfaces.LessonCellService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.sentry.Sentry;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class TsuDbImporterComponent {
	private static final Logger log = LoggerFactory.getLogger(TsuDbImporterComponent.class);
	private final AtomicBoolean isUpdatingLocalDatabase;
	private ImportStrategy strategy;
	private String currentSeason;
	private String currentSemester;
	private LessonCellService lessonCellService;
	private ObjectMapper objectMapper;

	@Autowired
	public TsuDbImporterComponent(ImportStrategy strategy,
								  @Value("${app.tsudb.current-season}") String currentSeason,
								  @Value("${app.tsudb.semester}") String currentSemester,
								  LessonCellService lessonCellService) {
		this.strategy = strategy;
		this.lessonCellService = lessonCellService;
		this.isUpdatingLocalDatabase = new AtomicBoolean(false);
		this.objectMapper = new ObjectMapper()
			.registerModule(new Jdk8Module())
			.registerModule(new JavaTimeModule());
		this.currentSeason = currentSeason;
		this.currentSemester = currentSemester;
	}

	private Set<PropertyInfo<IdExternal>> findAnnotatedWithIdExternal(Class<?> clazz) {
		Set<PropertyInfo<IdExternal>> output = new HashSet<>();
		for (Field field : clazz.getDeclaredFields()) {
			try {
				if (field.isAnnotationPresent(IdExternal.class)) {
					PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(clazz, field.getName());
					IdExternal idExternal = field.getAnnotation(IdExternal.class);
					Method setter = clazz.getMethod(idExternal.toSetter(), idExternal.clazz());
					output.add(new PropertyInfo<>(idExternal, pd.getReadMethod(), setter));
				}
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
		return output;
	}

	@Scheduled(cron = "${app.tsudb.cron}")
	public void importExternalModels() {
		try {
			importExternalModels(false);
		} catch (Exception e) {
			log.error("Error during importing", e);
			Sentry.capture(e);
			throw new RuntimeException(e);
		}
	}

	private void setTimes(LessonCell cell, TimeSchedule times) {
		String rawTime = times.getSchedule().get(cell.getColumnPosition());
		String startTime = rawTime.split("-")[0];
		if (startTime.length() != 5) { // 8:30 -> 08:30
			startTime = "0" + startTime;
		}
		String endTime = rawTime.split("-")[1];
		if (endTime.length() != 5) {
			endTime = "0" + endTime;
		}
		cell.setStart(LocalTime.parse(startTime));
		cell.setEnd(LocalTime.parse(endTime));
	}

	private TimeSchedule getMostPopular(Map<TimeSchedule, Integer> map) {
		int max = 0;
		TimeSchedule timeSchedule = null;
		for (Map.Entry<TimeSchedule, Integer> entry : map.entrySet()) {
			if (entry.getValue() > max) {
				max = entry.getValue();
				timeSchedule = entry.getKey();
			}
		}
		return timeSchedule;
	}

	/**
	 * Imports LessonCells from an external database and merges changes with the local database
	 *
	 * @param overrideCache ask ImportStrategy to drop cache or not
	 * @throws ImportException if any error happen during import
	 */
	public synchronized void importExternalModels(boolean overrideCache) throws ImportException {
		log.info("Starting importing");
		// Step 1: load all seasons
		List<Season> seasons = getSeasons(overrideCache);
		log.info("Seasons loaded");
		// we don't care about old data
		List<Season> filteredSeasons = filterSeasons(seasons);
		// Step 2: find all @IdExternal (id's in Season children to some objects in external db)
		// and fill fields with data from strategy
		int counter = crawl(filteredSeasons);
		log.info("Imported {} items", counter);
		// Step 3: convert data to LessonCell
		Set<LessonCell> preparedCells = getLessonCells(filteredSeasons);
		// Step 4: update local db
		rebuildDatabase(preparedCells);
	}

	/**
	 * Get all Seasons from the ImportStrategy
	 *
	 * @param overrideCache flag to drop cache in the ImportStrategy
	 * @return list of Seasons, raw presentation
	 * @throws ImportException when IOException occurs
	 */
	private List<Season> getSeasons(boolean overrideCache) throws ImportException {
		List<Season> seasons;
		if (overrideCache) {
			try {
				strategy.dropCache();
			} catch (IOException e) {
				throw new ImportException("Drop cache error", e);
			}
		}
		try {
			String allSeasonsRaw = strategy.load("timetables", true);
			seasons = objectMapper.readValue(allSeasonsRaw, new TypeReference<List<Season>>() {
			});
		} catch (IOException e) {
			throw new ImportException("Decoding seasons error", e);
		}
		return seasons;
	}

	/**
	 * Returns filtered Seasons, according to year and semester
	 *
	 * @param seasons list of Seasons to be filtered
	 * @return relevant Seasons
	 */
	private List<Season> filterSeasons(List<Season> seasons) {
		return seasons.stream()
			.filter(season -> season.get_id().getYear().equals(currentSeason))
			.filter(season -> Objects.equals(season.get_id().getSemester().replace('O', 'О'), currentSemester)) // eng O -> rus O, costul'
			.collect(Collectors.toList());
	}

	/**
	 * Scans all children-objects at Seasons and fill them with data from an ImportStrategy.
	 * <p>
	 * Uses a DFS tree traversal algorithm: a Season is a tree-like object.
	 * Algorithm search all annotated with @IdExternal fields and tries to fill a related
	 * field.
	 *
	 * @param filteredSeasons seasons to scan and fill
	 * @return counter of scanned objects
	 * @throws ImportException when IOException, reflection, cast and logic exception occurs
	 */
	private int crawl(List<Season> filteredSeasons) throws ImportException {
		Stack<Object> stack = new Stack<>();
		stack.addAll(filteredSeasons);
		int counter = 0;
		Set<String> reloadedMutableObjects = new HashSet<>();
		while (!stack.empty()) {
			Object o = stack.pop();
			counter++;
			if (counter > 350000) { // in case of ringing
				throw new ImportException("Stack is filled with garbage");
			}
			if (o == null) continue;
			Class<?> clazz = o.getClass();

			// Step 1: fill data from external ids (load from strategy)
			Set<PropertyInfo<IdExternal>> annotated = findAnnotatedWithIdExternal(clazz);
			for (PropertyInfo<IdExternal> pi : annotated) {
				try {
					Object info = pi.infoGetter.invoke(o);
					if (info != null) {
						String path = pi.annotation.url() + '/' + info.toString();
						boolean mutable = pi.annotation.mutable();
						String rawData;
						if (mutable && !reloadedMutableObjects.contains(path)) {
							rawData = strategy.load(path, true);
							reloadedMutableObjects.add(path);
						} else {
							rawData = strategy.load(path, false);
						}
						Object o1 = objectMapper.readValue(rawData, pi.getAnnotation().clazz());
						pi.dataSetter.invoke(o, o1);
					}
				} catch (InvocationTargetException | IllegalAccessException | IOException e) {
					throw new ImportException(String.format("Data injection error, class:%s", clazz.toString()), e);
				}
			}
			if (counter % 500 == 0) log.debug("counter:{} class:{}", counter, clazz.toString());
			// Step 2: find next objects (invoke all getters to external models from 'externalmodels' module)
			for (PropertyDescriptor propertyDescriptor : BeanUtils.getPropertyDescriptors(o.getClass())) {
				Class<?> returnClazz = propertyDescriptor.getPropertyType();
				if (returnClazz.getModule().equals(o.getClass().getModule())) {
					try {
						stack.add(propertyDescriptor.getReadMethod().invoke(o));
					} catch (IllegalAccessException | InvocationTargetException e) {
						throw new ImportException(
							String.format("Getter invocation error, getter:%s of object of class:%s, superclass:%s, counter:%d",
								propertyDescriptor.getReadMethod() != null ? propertyDescriptor.getReadMethod().toString() : "null",
								returnClazz.toString(), clazz.toString(), counter), e);
					}
				} else if (Collection.class.isAssignableFrom(returnClazz)) {
					try {
						Collection<?> collection = (Collection<?>) propertyDescriptor.getReadMethod().invoke(o);
						// we have to check, what class is in collection (assumption that all have same class)
						// there's no double collections in externalmodels, so checking only module
						stack.addAll(collection.stream().filter(obj -> {
							if (obj == null) return false;
							Class<?> c = obj.getClass();
							return c.getModule().equals(o.getClass().getModule());
						}).collect(Collectors.toList()));
					} catch (IllegalAccessException | InvocationTargetException e) {
						throw new ImportException(
							String.format("Getter invocation error (collection), getter:%s of object of class:%s, superclass:%s, counter:%d",
								propertyDescriptor.getReadMethod() != null ? propertyDescriptor.getReadMethod().toString() : "null",
								returnClazz.toString(), clazz.toString(), counter), e);
					}
				}
			}
		}
		return counter;
	}

	/**
	 * Convert Seasons to valid, database-ready LessonCells
	 *
	 * @param filteredSeasons Seasons to convert
	 * @return set of LessonCell
	 */
	private Set<LessonCell> getLessonCells(List<Season> filteredSeasons) {
		Set<LessonCell> preparedCells = new HashSet<>();
		Map<String, Map<TimeSchedule, Integer>> defaultTimeScheduleMap = new HashMap<>();
		// Step 1: convert Seasons to LessonCells
		for (Season season : filteredSeasons) {
			for (Timetable timetable : season.getTables()) {
				for (TimetableCell cell : timetable.getCells()) {
					for (Lesson lesson : cell.getLessons()) {
						try {
							LessonCell lessonCell = new LessonCell();
							lessonCell.setExternalId(lesson.get_id());
							switch (lesson.getPlus_minus()) {
								case "+":
									lessonCell.setWeekSign(WeekSign.PLUS);
									break;
								case "-":
									lessonCell.setWeekSign(WeekSign.MINUS);
									break;
								default:
									lessonCell.setWeekSign(WeekSign.ANY);
									break;
							}
							// exists lessons, which contains only comment or nothing.
							// if comment provided, use it as subject
							// else drop this lesson
							if (lesson.getSubjectObj() != null) {
								lessonCell.setFullSubjectName(lesson.getSubjectObj().getName());
								lessonCell.setShortSubjectName(lesson.getSubjectObj().getAbbr());
								String rawComment = lesson.getComment();
								if (rawComment != null && rawComment.matches("^\\(.+\\)$")) {
									lessonCell.setAttributes(
										Arrays.stream(rawComment
											.substring(1, rawComment.length() - 1)
											.split(","))
											.map(String::strip)
											.collect(Collectors.toList())
									);
								}
							} else {
								lessonCell.setFullSubjectName(lesson.getComment());
								lessonCell.setShortSubjectName(lesson.getComment());
							}
							if (lesson.getTeacherObj() != null) {
								lessonCell.setTeacherName(lesson.getTeacherObj().getFio());
								lessonCell.setTeacherTitle(lesson.getTeacherObj().getPost());
							}
							if (lesson.getAuditoryObj() != null) {
								lessonCell.setAuditoryAddress(lesson.getAuditoryObj().getHousing() + "|" +
									lesson.getAuditoryObj().getName());
							}
							lessonCell.setDayOfWeek(cell.getDay().getJavaDayOfWeek());
							lessonCell.setColumnPosition(cell.getNumber());
							String faculty = season.get_id().getFaculty().getAbbr();
							if (timetable.getTimeSchedule() != null) {
								defaultTimeScheduleMap.putIfAbsent(faculty, new HashMap<>());
								defaultTimeScheduleMap.get(faculty).putIfAbsent(timetable.getTimeSchedule(), 0);
								int prev = defaultTimeScheduleMap.get(faculty).get(timetable.getTimeSchedule());
								defaultTimeScheduleMap.get(faculty).put(timetable.getTimeSchedule(), prev + 1);
								setTimes(lessonCell, timetable.getTimeSchedule());
							}
							lessonCell.setLevel(timetable.getDirection().getLevel());
							lessonCell.setCourse(timetable.getCourse());
							// drop non-informative lessons
							if (timetable.getGroupName() == null) {
								continue;
							}
							lessonCell.setGroup(timetable.getGroupName().replace('"', '\''));
							lessonCell.setSubgroup(lesson.getSubgroup());
							lessonCell.setCountOfSubgroups(timetable.getSubgroups().size());
							lessonCell.setFaculty(faculty);
							// drop empty lessons
							if (lessonCell.getFullSubjectName() == null && lessonCell.getShortSubjectName() == null) {
								continue;
							}
							preparedCells.add(lessonCell);
						} catch (Exception e) {
							log.error("Error while transfering to LessonCell, faculty {}",
								season.get_id().getFaculty().getAbbr());
							log.trace("Error trace: ", e);
						}
					}
				}
			}
		}
		// Step 2: set CrossPair flags and set times, if it's not presented
		Map<CrossPairLessonCellCoordinates, LessonCell> firstOccurrences = new HashMap<>();
		for (LessonCell lessonCell : preparedCells) {
			CrossPairLessonCellCoordinates crossPair = CrossPairLessonCellCoordinates.convert(lessonCell);
			if (firstOccurrences.containsKey(crossPair)) {
				firstOccurrences.get(crossPair).setCrossPair(true);
				lessonCell.setCrossPair(true);
			} else {
				firstOccurrences.put(crossPair, lessonCell);
				lessonCell.setCrossPair(false);
			}
			if (lessonCell.getStart() == null) {
				setTimes(lessonCell, getMostPopular(defaultTimeScheduleMap.get(lessonCell.getFaculty())));
			}
		}
		return preparedCells;
	}

	/**
	 * Merges local database and new LessonCells.
	 * <p>
	 * This method does a lookup of every LessonCell in the local database.
	 * LessonCell, which origin is an external database, will be overridden by the new
	 * version or dropped if preparedCells don't contain a correspondent LessonCell.
	 * User-made LessonCell will be kept or dropped according to with flags ignoreExternalDb
	 * and ignoreExternalDbHashCode.
	 * <p>
	 * The method does not allow concurrent calls.
	 *
	 * @param preparedCells LessonCells from external database
	 * @throws ImportException if the local database is locked by another call or if an
	 *                         Exception occurs
	 */
	private void rebuildDatabase(Set<LessonCell> preparedCells) throws ImportException {
		// if new LessonCell, save it
		// if updated LessonCell (db contains entity with same id), update it
		// if LessonCell from local db hasn't double from external id, delete it
		if (!this.isUpdatingLocalDatabase.compareAndSet(false, true)) {
			throw new ImportException("Database already in use!");
		}
		try {
			Set<LessonCell> allCellsInDb = lessonCellService.getAll();
			Map<String, LessonCell> idToCellInDb = allCellsInDb.stream()
				.collect(Collectors.toMap(LessonCell::getExternalId, Function.identity()));
			// catch updated LessonCells
			Set<LessonCell> localCellsToMakePatches = preparedCells.stream()
				.filter(lessonCell -> idToCellInDb.containsKey(lessonCell.getExternalId()))
				.collect(Collectors.toSet());
			Set<LessonCell> localUpdatedLessonCell = new HashSet<>();
			Set<LessonCell> localNewLessonCells = new HashSet<>(preparedCells);
			Set<LessonCell> remainingDbCells = new HashSet<>(allCellsInDb);
			for (LessonCell preparedCell : localCellsToMakePatches) {
				LessonCell inDb = idToCellInDb.get(preparedCell.getExternalId());
				remainingDbCells.remove(inDb);
				localNewLessonCells.remove(preparedCell);
				inDb.transfer(preparedCell);
				localUpdatedLessonCell.add(inDb);
			}
			// Step 5.1: user-created LessonCells
			// if ignoreExternalDb and ignoreExternalDbHashCode == null, block external db's identical cell.
			// if ignoreExternalDb and ignoreExternalDbHashCode != null, check cell's hash code. if
			//    hashCode != ignoreExternalDbHashCode, drop user-created. Otherwise block external db's cell.
			// if not ignoreExternalDb and external db contains identical cell, drop user-created.
			//    Otherwise remain user-created.
			Set<LessonCell> userMadeCells = remainingDbCells.stream()
				.filter(lessonCell -> lessonCell.getCreator() != null)
				.collect(Collectors.toSet());
			remainingDbCells.removeAll(userMadeCells);
			Map<LessonCellCoordinates, LessonCell> userMadeCellsCoordinates = userMadeCells.stream()
				.collect(Collectors.toMap(LessonCellCoordinates::convert, Function.identity()));
			Map<LessonCellCoordinates, List<LessonCell>> importedCellsCoordinates = Stream.concat(localUpdatedLessonCell.stream(),
				localNewLessonCells.stream())
				.collect(Collectors.toMap(LessonCellCoordinates::convert,
					cell -> new LinkedList<>(Collections.singletonList(cell)),
					(l1, l2) -> {
						l1.addAll(l2);
						l2.clear();
						return l1;
					}
				));
			Set<LessonCellCoordinates> coordinatesIntersection = userMadeCellsCoordinates.keySet().stream()
				.filter(importedCellsCoordinates::containsKey)
				.collect(Collectors.toSet());
			for (LessonCellCoordinates intersection : coordinatesIntersection) {
				LessonCell userCreated = userMadeCellsCoordinates.get(intersection);
				List<LessonCell> importedCell = importedCellsCoordinates.get(intersection);
				String importedCellIds = importedCell.stream()
					.map(LessonCell::getExternalId)
					.sorted()
					.collect(Collectors.joining(","));
				int intersectionId = Math.abs(new Random().nextInt());
				String userCreatedId = userCreated.getExternalId();
				log.info("I{}. Intersection of user-created {} with LessonCells: {}",
					intersectionId,
					userCreatedId,
					importedCellIds);
				if (userCreated.getIgnoreExternalDb()) {
					if (userCreated.getIgnoreExternalDbHashCode() != null) {
						String importedCellHashCodeCompilation = importedCell.stream()
							.map(LessonCell::hashCode)
							.sorted()
							.map(hc -> Integer.toString(hc))
							.collect(Collectors.joining(","));
						if (!importedCellHashCodeCompilation.equals(userCreated.getIgnoreExternalDbHashCode())) {
							log.info("I{}. Drop user-created {}. Reason: user-created has outdated block: {} vs actual {}",
								intersectionId,
								userCreatedId,
								userCreated.getIgnoreExternalDbHashCode(),
								importedCellHashCodeCompilation);
							remainingDbCells.add(userCreated);
						} else {
							log.info("I{}. Block and drop LessonCells {}. Reason: user-created {} has hashCode-block {}",
								intersectionId,
								importedCellIds,
								userCreatedId,
								userCreated.getIgnoreExternalDbHashCode());
							localNewLessonCells.removeAll(importedCell);
							localUpdatedLessonCell.removeAll(importedCell);
						}
					} else {
						log.info("I{}. Block and drop LessonCells {}. Reason: user-created {} has absolute-block",
							intersectionId,
							importedCellIds,
							userCreatedId);
						localNewLessonCells.removeAll(importedCell);
						localUpdatedLessonCell.removeAll(importedCell);
					}
				} else {
					log.info("I{}. Drop user-created {}. Reason: not ignoreExternalDb",
						intersectionId,
						userCreatedId);
					remainingDbCells.add(userCreated);
				}
			}
			lessonCellService.saveAll(localUpdatedLessonCell);
			lessonCellService.saveAll(localNewLessonCells);
			lessonCellService.deleteAll(remainingDbCells);
			log.info("Transferring TsuDb data to local db completed, {} added, {} updated, {} deleted, {} total",
				localNewLessonCells.size(),
				localUpdatedLessonCell.size(),
				remainingDbCells.size(),
				lessonCellService.size());
		} catch (Exception e) {
			log.error("Error while transferring", e);
			throw new ImportException("Error while transferring", e);
		} finally {
			this.isUpdatingLocalDatabase.set(false);
		}
	}

	public boolean isUpdatingLocalDatabase() {
		return isUpdatingLocalDatabase.get();
	}

	@Data
	private final static class PropertyInfo<T> {
		final T annotation;
		final Method infoGetter;
		final Method dataSetter;
	}
}
