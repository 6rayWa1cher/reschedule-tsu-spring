package com.a6raywa1cher.rescheduletsuspring.components.tsudbimporter;

import com.a6raywa1cher.rescheduletsuspring.config.TsuDbImporterConfigProperties;
import com.a6raywa1cher.rescheduletsuspring.externalmodels.*;
import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import com.a6raywa1cher.rescheduletsuspring.models.WeekSign;
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

@Component
public class TsuDbImporterComponent {
	private static final Logger log = LoggerFactory.getLogger(TsuDbImporterComponent.class);
	private final AtomicBoolean isUpdatingLocalDatabase;
	private ImportStrategy strategy;
	private TsuDbImporterConfigProperties properties;
	private LessonCellService lessonCellService;

	@Autowired
	public TsuDbImporterComponent(ImportStrategy strategy, TsuDbImporterConfigProperties properties,
	                              LessonCellService lessonCellService) {
		this.strategy = strategy;
		this.properties = properties;
		this.lessonCellService = lessonCellService;
		this.isUpdatingLocalDatabase = new AtomicBoolean(false);
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

	@SuppressWarnings("DuplicatedCode")
	public synchronized void importExternalModels(boolean overrideCache) throws ImportException {
		ObjectMapper objectMapper = new ObjectMapper()
			.registerModule(new Jdk8Module())
			.registerModule(new JavaTimeModule());
		// Step 1: load all seasons
		log.info("Starting importing");
		List<Season> seasons;
		try {
			String allSeasonsRaw = strategy.load("timetables", true);
			seasons = objectMapper.readValue(allSeasonsRaw, new TypeReference<List<Season>>() {
			});
		} catch (IOException e) {
			throw new ImportException("Decoding seasons error", e);
		}
		log.info("Seasons loaded");
		// we don't care about old data
		List<Season> filteredSeasons = seasons.stream()
			.filter(season -> season.get_id().getYear().equals(properties.getCurrentSeason()))
			.collect(Collectors.toList());
		// Step 2: find all @IdExternal (id's in Season children to some objects in external db)
		// and fill fields with data from strategy
		Stack<Object> stack = new Stack<>();
		stack.addAll(filteredSeasons);
		int counter = 0;
		while (!stack.empty()) {
			Object o = stack.pop();
			counter++;
			if (counter > 350000) {
				throw new ImportException("Stack is filled with garbage");
			}
			if (o == null) continue;
			Class<?> clazz = o.getClass();

			// Step 2.1: fill data from external ids (load from strategy)
			Set<PropertyInfo<IdExternal>> annotated = findAnnotatedWithIdExternal(clazz);
			for (PropertyInfo<IdExternal> pi : annotated) {
				try {
					Object info = pi.infoGetter.invoke(o);
					if (info != null) {
						String rawData = strategy.load(pi.annotation.url() + '/' + info.toString(), overrideCache);
						Object o1 = objectMapper.readValue(rawData, pi.getAnnotation().clazz());
						pi.dataSetter.invoke(o, o1);
					}
				} catch (InvocationTargetException | IllegalAccessException | IOException e) {
					throw new ImportException(String.format("Data injection error, class:%s", clazz.toString()), e);
				}
			}
			if (counter % 500 == 0) log.info("counter:{} class:{}", counter, clazz.toString());
			// Step 2.2: find next objects (invoke all getters to external models from 'externalmodels' module)
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
		log.info("Imported {} items", counter);
		// Step 3: convert data to LessonCell
		Set<LessonCell> preparedCells = new HashSet<>();
		Map<String, Map<TimeSchedule, Integer>> defaultTimeScheduleMap = new HashMap<>();
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
//							defaultTimeScheduleMap.putIfAbsent(season.get_id().getFaculty().getAbbr(), timetable.getTimeSchedule());
								defaultTimeScheduleMap.putIfAbsent(faculty, new HashMap<>());
								defaultTimeScheduleMap.get(faculty).putIfAbsent(timetable.getTimeSchedule(), 0);
								int prev = defaultTimeScheduleMap.get(faculty).get(timetable.getTimeSchedule());
								defaultTimeScheduleMap.get(faculty).put(timetable.getTimeSchedule(), prev + 1);
//							String rawTime = timetable.getTimeSchedule().getSchedule().get(cell.getNumber());
								setTimes(lessonCell, timetable.getTimeSchedule());
							}
							lessonCell.setLevel(timetable.getDirection().getLevel());
							lessonCell.setCourse(timetable.getCourse());
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
						}
					}
				}
			}
		}
		// Step 4: set CrossPair flags and set times, if it's not presented
		Map<CrossPairLessonCell, LessonCell> firstOccurrences = new HashMap<>();
		for (LessonCell lessonCell : preparedCells) {
			CrossPairLessonCell crossPair = CrossPairLessonCell.convert(lessonCell);
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
		firstOccurrences.clear();
		System.gc();

		// Step 5: update local db
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
			Set<LessonCell> intersection = preparedCells.stream()
				.filter(lessonCell -> idToCellInDb.containsKey(lessonCell.getExternalId()))
				.collect(Collectors.toSet());
			Set<LessonCell> toPull = new HashSet<>();
			Set<LessonCell> remainingPreparedCells = new HashSet<>(preparedCells);
			Set<LessonCell> remainingDbCells = new HashSet<>(allCellsInDb);
			for (LessonCell preparedCell : intersection) {
				LessonCell inDb = idToCellInDb.get(preparedCell.getExternalId());
				remainingDbCells.remove(inDb);
				remainingPreparedCells.remove(preparedCell);
				inDb.setWeekSign(preparedCell.getWeekSign());
				inDb.setFullSubjectName(preparedCell.getFullSubjectName());
				inDb.setShortSubjectName(preparedCell.getShortSubjectName());
				inDb.setTeacherName(preparedCell.getTeacherName());
				inDb.setTeacherTitle(preparedCell.getTeacherTitle());
				inDb.setDayOfWeek(preparedCell.getDayOfWeek());
				inDb.setColumnPosition(preparedCell.getColumnPosition());
				inDb.setStart(preparedCell.getStart());
				inDb.setEnd(preparedCell.getEnd());
				inDb.setAuditoryAddress(preparedCell.getAuditoryAddress());
				inDb.setLevel(preparedCell.getLevel());
				inDb.setCourse(preparedCell.getCourse());
				inDb.setGroup(preparedCell.getGroup());
				inDb.setSubgroup(preparedCell.getSubgroup());
				inDb.setCountOfSubgroups(preparedCell.getCountOfSubgroups());
				inDb.setCrossPair(preparedCell.getCrossPair());
				inDb.setFaculty(preparedCell.getFaculty());
				toPull.add(inDb);
			}
			lessonCellService.saveAll(toPull);
			lessonCellService.saveAll(remainingPreparedCells);
			lessonCellService.deleteAll(remainingDbCells);
			log.info("Transferring TsuDb data to local db completed, {} added, {} updated, {} deleted", remainingPreparedCells.size(), toPull.size(), remainingDbCells.size());
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
