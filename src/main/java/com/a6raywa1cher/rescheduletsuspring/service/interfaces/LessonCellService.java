package com.a6raywa1cher.rescheduletsuspring.service.interfaces;

import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import com.a6raywa1cher.rescheduletsuspring.models.User;
import com.a6raywa1cher.rescheduletsuspring.models.submodels.LessonCellCoordinates;
import com.a6raywa1cher.rescheduletsuspring.service.submodels.DaySchedule;
import com.a6raywa1cher.rescheduletsuspring.service.submodels.GroupInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.stream.Stream;

public interface LessonCellService {
	Set<LessonCell> getAll();

	Optional<LessonCell> getById(String id);

	Page<LessonCell> getByUser(User user, Pageable pageable);

	List<DaySchedule> getReadySchedules(String faculty, String group, Date from, Integer days);

	List<LessonCell> getTeacherRaw(String teacherName);

	List<DaySchedule> getReadyTeacherSchedules(String teacherName, Date from, Integer days);

	List<GroupInfo> findGroupsAndSubgroups(String faculty);

	List<String> getTeachersWithName(String teacherName);

	List<String> getAllFaculties();

	List<LessonCell> getAllByGroup(String group, String faculty);

	LessonCell addUserCell(LessonCell cell, boolean ignoreLastExternalDbRecord);

	Stream<LessonCell> getByLessonCellCoordinates(LessonCellCoordinates coordinates);

	Map<String, Long> getLeaderBoard();

	Iterable<LessonCell> saveAll(Collection<LessonCell> collection);

	void deleteAll(Collection<LessonCell> collection);
}
