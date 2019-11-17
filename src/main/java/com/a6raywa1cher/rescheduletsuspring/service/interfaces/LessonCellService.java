package com.a6raywa1cher.rescheduletsuspring.service.interfaces;

import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import com.a6raywa1cher.rescheduletsuspring.service.submodels.DaySchedule;
import com.a6raywa1cher.rescheduletsuspring.service.submodels.GroupInfo;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

public interface LessonCellService {
	Set<LessonCell> getAll();

	List<DaySchedule> getReadySchedules(String faculty, String group, int semester, Date from, Integer days);

	List<LessonCell> getTeacherRaw(String teacherName);

	List<DaySchedule> getReadyTeacherSchedules(String teacherName, Date from, Integer days);

	List<GroupInfo> findGroupsAndSubgroups(String faculty, int semester);

	List<String> getTeachersWithName(String teacherName);

	List<String> getAllFaculties();

	List<LessonCell> getAllByGroup(String group, String faculty, int semester);

	Iterable<LessonCell> saveAll(Collection<LessonCell> collection);

	int getCurrentSemester(String facultyId);

	void deleteAll(Collection<LessonCell> collection);
}
