package com.a6raywa1cher.rescheduletsuspring.components;

import com.a6raywa1cher.rescheduletsuspring.config.TsuDbImporterConfigProperties;
import com.a6raywa1cher.rescheduletsuspring.dao.interfaces.LessonCellService;
import com.a6raywa1cher.rescheduletsuspring.externalmodels.*;
import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import com.a6raywa1cher.rescheduletsuspring.models.Week;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TsuDbImporter {
	private static final Logger log = LoggerFactory.getLogger(TsuDbImporter.class);
	private ImportStrategy strategy;
	private TsuDbImporterConfigProperties properties;
	private LessonCellService lessonCellService;

	@Autowired
	public TsuDbImporter(ImportStrategy strategy, TsuDbImporterConfigProperties properties,
	                     LessonCellService lessonCellService) {
		this.strategy = strategy;
		this.properties = properties;
		this.lessonCellService = lessonCellService;
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

	public void importExternalModels() {
		ObjectMapper objectMapper = new ObjectMapper()
				.registerModule(new Jdk8Module())
				.registerModule(new JavaTimeModule());
		String allSeasonsRaw = strategy.load("timetables");
		List<Season> seasons;
		try {
			seasons = objectMapper.readValue(allSeasonsRaw, new TypeReference<List<Season>>() {
			});
		} catch (IOException e) {
			log.error("Decoding error", e);
			return;
		}
		List<Season> filteredSeasons = seasons.stream()
				.filter(season -> season.get_id().getYear().equals(properties.getCurrentSeason()))
				.collect(Collectors.toList());
		Stack<Object> stack = new Stack<>();
		stack.addAll(filteredSeasons);
		int counter = 0;
		while (!stack.empty()) {
			Object o = stack.pop();
			counter++;
			if (o == null) continue;
			Class<?> clazz = o.getClass();

			// step 1: fill data from external ids (load from strategy)
			Set<PropertyInfo<IdExternal>> annotated = findAnnotatedWithIdExternal(clazz);
			for (PropertyInfo<IdExternal> pi : annotated) {
				try {
					Object info = pi.infoGetter.invoke(o);
					if (info != null) {
						String rawData = strategy.load(pi.annotation.url() + '/' + info.toString());
						Object o1 = objectMapper.readValue(rawData, pi.getAnnotation().clazz());
						pi.dataSetter.invoke(o, o1);
					}
				} catch (InvocationTargetException | IllegalAccessException | IOException e) {
					log.error("Data injection error", e);
				}
			}
			if (counter % 100 == 0) log.info("counter:{} class:{}", counter, clazz.toString());
			// step 2: find next objects (invoke all getters to external models)
			for (PropertyDescriptor propertyDescriptor : BeanUtils.getPropertyDescriptors(o.getClass())) {
				Class<?> returnClazz = propertyDescriptor.getPropertyType();
				if (returnClazz.getModule().equals(o.getClass().getModule())) {
					try {
						stack.add(propertyDescriptor.getReadMethod().invoke(o));
					} catch (IllegalAccessException | InvocationTargetException e) {
						log.error("Getter invokation error", e);
					}
				} else if (Collection.class.isAssignableFrom(returnClazz)) {
					try {
						Collection<?> collection = (Collection<?>) propertyDescriptor.getReadMethod().invoke(o);
						Object anyObject = collection.size() > 0 ? collection.iterator().next() : "";
						if (anyObject == null) continue;
						if (anyObject.getClass().getModule().equals(o.getClass().getModule()))
							stack.addAll(collection.stream().filter(obj -> {
								Class<?> c = obj.getClass();
								return c.getModule().equals(o.getClass().getModule()) ||
										Collection.class.isAssignableFrom(returnClazz);
							}).collect(Collectors.toList()));
					} catch (IllegalAccessException | InvocationTargetException e) {
						log.error("Getter invokation error (collection)", e);
					}
				}
			}
		}
		log.info("Imported {} items", counter);
		Set<LessonCell> preparedCells = new HashSet<>();
		for (Season season : filteredSeasons) {
			for (Timetable timetable : season.getTables()) {
				for (TimetableCell cell : timetable.getCells()) {
					for (Lesson lesson : cell.getLessons()) {
						LessonCell lessonCell = new LessonCell();
						lessonCell.setExternalId(lesson.get_id());
						switch (lesson.getPlus_minus()) {
							case "+":
								lessonCell.setWeek(Week.PLUS);
								break;
							case "-":
								lessonCell.setWeek(Week.MINUS);
								break;
							default:
								lessonCell.setWeek(Week.ANY);
								break;
						}
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
						String rawTime = timetable.getTimeSchedule().getSchedule().get(cell.getNumber());
						String startTime = rawTime.split("-")[0];
						if (startTime.length() != 5) {
							startTime = "0" + startTime;
						}
						String endTime = rawTime.split("-")[1];
						if (endTime.length() != 5) {
							endTime = "0" + endTime;
						}
						lessonCell.setStart(LocalTime.parse(startTime));
						lessonCell.setEnd(LocalTime.parse(endTime));
						lessonCell.setGroup(timetable.getGroupName());
						lessonCell.setSubgroup(lesson.getSubgroup());
						lessonCell.setCountOfSubgroups(timetable.getSubgroups().size());
						lessonCell.setFaculty(season.get_id().getFaculty().getAbbr());
						if (lessonCell.getFullSubjectName() == null && lessonCell.getShortSubjectName() == null) {
							continue;
						}
						preparedCells.add(lessonCell);
					}
				}
			}
		}
		Set<LessonCell> allCells = lessonCellService.getAll();
		Map<String, LessonCell> idToCellInDb = allCells.parallelStream()
				.collect(Collectors.toMap(LessonCell::getExternalId, Function.identity()));
		// update existing
		Set<LessonCell> intersection = preparedCells.stream()
				.filter(lessonCell -> idToCellInDb.containsKey(lessonCell.getExternalId()))
				.collect(Collectors.toSet());
		Set<LessonCell> toPull = new HashSet<>();
		Set<LessonCell> remainingPreparedCells = new HashSet<>(preparedCells);
		Set<LessonCell> remainingDbCells = new HashSet<>(allCells);
		for (LessonCell preparedCell : intersection) {
			LessonCell inDb = idToCellInDb.get(preparedCell.getExternalId());
			remainingDbCells.remove(inDb);
			remainingPreparedCells.remove(preparedCell);
			inDb.setWeek(preparedCell.getWeek());
			inDb.setFullSubjectName(preparedCell.getFullSubjectName());
			inDb.setShortSubjectName(preparedCell.getShortSubjectName());
			inDb.setTeacherName(preparedCell.getTeacherName());
			inDb.setTeacherTitle(preparedCell.getTeacherTitle());
			inDb.setDayOfWeek(preparedCell.getDayOfWeek());
			inDb.setColumnPosition(preparedCell.getColumnPosition());
			inDb.setStart(preparedCell.getStart());
			inDb.setEnd(preparedCell.getEnd());
			inDb.setAuditoryAddress(preparedCell.getAuditoryAddress());
			inDb.setGroup(preparedCell.getGroup());
			inDb.setSubgroup(preparedCell.getSubgroup());
			inDb.setCountOfSubgroups(preparedCell.getCountOfSubgroups());
			inDb.setFaculty(preparedCell.getFaculty());
			toPull.add(inDb);
		}
		lessonCellService.saveAll(toPull);
		lessonCellService.saveAll(remainingPreparedCells);
		lessonCellService.deleteAll(remainingDbCells);
		log.info("Transferring TsuDb data to local db completed");
	}

	@Data
	private final static class PropertyInfo<T> {
		final T annotation;
		final Method infoGetter;
		final Method dataSetter;
	}
}
