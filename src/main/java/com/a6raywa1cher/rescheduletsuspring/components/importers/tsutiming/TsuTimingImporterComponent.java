package com.a6raywa1cher.rescheduletsuspring.components.importers.tsutiming;

import com.a6raywa1cher.rescheduletsuspring.components.importers.AbstractExternalModelsImporter;
import com.a6raywa1cher.rescheduletsuspring.components.importers.ImportException;
import com.a6raywa1cher.rescheduletsuspring.components.importers.LessonCellSynchronizationService;
import com.a6raywa1cher.rescheduletsuspring.components.importers.enhancer.LessonCellEnhancerService;
import com.a6raywa1cher.rescheduletsuspring.components.importers.loader.DataLoader;
import com.a6raywa1cher.rescheduletsuspring.components.importers.tsutiming.models.*;
import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import com.a6raywa1cher.rescheduletsuspring.models.WeekSign;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@ConditionalOnExpression("${app.tsudb.enabled:true} && '${app.tsudb.remote-type:timetable}' == 'timing'")
@Slf4j
public class TsuTimingImporterComponent extends AbstractExternalModelsImporter {
	private final ObjectMapper objectMapper = new ObjectMapper()
		.registerModule(new Jdk8Module())
		.registerModule(new JavaTimeModule());

	private final DataLoader dataLoader;

	private final String currentSeason;

	private final String currentSemester;

	private final LessonCellSynchronizationService lessonCellSynchronizationService;

	private final LessonCellEnhancerService lessonCellEnhancerService;

	@Autowired
	public TsuTimingImporterComponent(
		DataLoader dataLoader,
		@Value("${app.tsudb.current-season}") String currentSeason,
		@Value("${app.tsudb.semester}") String currentSemester,
		LessonCellSynchronizationService lessonCellSynchronizationService,
		LessonCellEnhancerService lessonCellEnhancerService
	) {
		this.dataLoader = dataLoader;
		this.currentSeason = currentSeason;
		this.currentSemester = currentSemester;
		this.lessonCellSynchronizationService = lessonCellSynchronizationService;
		this.lessonCellEnhancerService = lessonCellEnhancerService;
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
	@Override
	protected void internalImportExternalModels(boolean overrideCache) throws ImportException {
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
				dataLoader.dropCache();
			} catch (IOException e) {
				throw new ImportException("Drop cache error", e);
			}
		}
		try {
			String allSeasonsRaw = dataLoader.load("timetables", true);
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
			.filter(season -> {
				String semester = season.get_id().getSemester();
				if (semester == null) return false;

				// eng O -> rus O, costul'
				return Objects.equals(semester.replace('O', 'О'), currentSemester);
			})
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
						String path = pi.annotation.url() + '/' + info;
						boolean mutable = pi.annotation.mutable();
						String rawData;
						if (mutable && !reloadedMutableObjects.contains(path)) {
							rawData = dataLoader.load(path, true);
							reloadedMutableObjects.add(path);
						} else {
							rawData = dataLoader.load(path, false);
						}
						Object o1 = objectMapper.readValue(rawData, pi.getAnnotation().clazz());
						pi.dataSetter.invoke(o, o1);
					}
				} catch (InvocationTargetException | IllegalAccessException | IOException e) {
					throw new ImportException(String.format("Data injection error, class:%s", clazz), e);
				}
			}
			if (counter % 500 == 0) log.debug("counter:{} class:{}", counter, clazz);
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
								returnClazz, clazz, counter), e);
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
								returnClazz, clazz, counter), e);
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
		// Step 2: set times, if it's not presented
		for (LessonCell lessonCell : preparedCells) {
			if (lessonCell.getStart() == null) {
				setTimes(lessonCell, getMostPopular(defaultTimeScheduleMap.get(lessonCell.getFaculty())));
			}
		}
		return new HashSet<>(lessonCellEnhancerService.enhance(preparedCells));
	}

	protected void rebuildDatabase(Set<LessonCell> preparedCells) throws ImportException {
		try {
			lessonCellSynchronizationService.rebuildDatabase(preparedCells);
		} catch (Exception e) {
			throw new ImportException(e.getMessage(), e);
		}
	}

	public boolean isBusy() {
		return super.isBusy() || lessonCellSynchronizationService.isUpdatingLocalDatabase();
	}

	@Data
	private final static class PropertyInfo<T> {
		final T annotation;
		final Method infoGetter;
		final Method dataSetter;
	}
}
