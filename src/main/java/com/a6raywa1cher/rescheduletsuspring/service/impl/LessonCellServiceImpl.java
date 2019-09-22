package com.a6raywa1cher.rescheduletsuspring.service.impl;

import com.a6raywa1cher.rescheduletsuspring.components.weeksign.WeekSignComponent;
import com.a6raywa1cher.rescheduletsuspring.dao.repository.LessonCellRepository;
import com.a6raywa1cher.rescheduletsuspring.dao.results.FindGroupsAndSubgroupsResult;
import com.a6raywa1cher.rescheduletsuspring.dao.results.FindTeacherResult;
import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import com.a6raywa1cher.rescheduletsuspring.models.WeekSign;
import com.a6raywa1cher.rescheduletsuspring.service.interfaces.LessonCellService;
import com.a6raywa1cher.rescheduletsuspring.service.submodels.DaySchedule;
import com.a6raywa1cher.rescheduletsuspring.service.submodels.GroupInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LessonCellServiceImpl implements LessonCellService {
	private LessonCellRepository repository;
	private WeekSignComponent weekSignComponent;

	@Autowired
	public LessonCellServiceImpl(LessonCellRepository repository, WeekSignComponent weekSignComponent) {
		this.repository = repository;
		this.weekSignComponent = weekSignComponent;
	}

	private List<DaySchedule> convertToReadySchedule(List<LessonCell> allCells, WeekSign currentWeekSign, Date from, Integer days) {
		Map<Pair<WeekSign, DayOfWeek>, DaySchedule> map = new LinkedHashMap<>();
		List<Pair<WeekSign, DayOfWeek>> daysList = new ArrayList<>();
		DayOfWeek currentDayOfWeek = LocalDate.ofInstant(from.toInstant(), ZoneId.systemDefault()).getDayOfWeek();
		while (daysList.size() != days) {
			if (currentDayOfWeek == DayOfWeek.SUNDAY) {
				currentDayOfWeek = DayOfWeek.MONDAY;
				currentWeekSign = WeekSign.inverse(currentWeekSign);
				continue;
			}
			DaySchedule daySchedule = new DaySchedule();
			daySchedule.setDayOfWeek(currentDayOfWeek);
			daySchedule.setSign(currentWeekSign);
			daySchedule.setCells(new ArrayList<>());
			map.put(Pair.of(currentWeekSign, currentDayOfWeek), daySchedule);
			daysList.add(Pair.of(currentWeekSign, currentDayOfWeek));
			currentDayOfWeek = DayOfWeek.values()[currentDayOfWeek.ordinal() + 1];
		}
		for (LessonCell cell : allCells) {
			for (Pair<WeekSign, DayOfWeek> pair : daysList) {
				if (!cell.getDayOfWeek().equals(pair.getSecond())) continue;
				if (cell.getWeekSign() == WeekSign.ANY || pair.getFirst().equals(cell.getWeekSign())) {
					map.get(pair).getCells().add(cell);
				}
			}
		}
		return new ArrayList<>(map.values());
	}

	@Override
	public Set<LessonCell> getAll() {
		return repository.getAllBy();
	}

	@Override
	public List<DaySchedule> getReadySchedules(String faculty, String group, Date from, Integer days) {
		List<LessonCell> allCells = this.getAllByGroup(group, faculty);
		WeekSign weekSign = weekSignComponent.getWeekSign(from, faculty);
		return convertToReadySchedule(allCells, weekSign, from, days);
	}

	@Override
	public List<LessonCell> getTeacherRaw(String teacherName) {
		return repository.getAllByTeacherName(teacherName);
	}

	@Override
	public List<DaySchedule> getReadyTeacherSchedules(String teacherName, Date from, Integer days) {
		List<LessonCell> allCells = getTeacherRaw(teacherName);
		if (allCells.isEmpty()) {
			return new ArrayList<>();
		}
		WeekSign weekSign = weekSignComponent.getWeekSign(from, allCells.get(0).getFaculty());
		return convertToReadySchedule(allCells, weekSign, from, days);
	}

	@Override
	public List<GroupInfo> findGroupsAndSubgroups(String faculty) {
		List<GroupInfo> groupInfos = new ArrayList<>();
		for (FindGroupsAndSubgroupsResult result : repository.findGroupsAndSubgroups(faculty)) {
			GroupInfo groupInfo = new GroupInfo();
			groupInfo.setName(result.getGroup());
			groupInfo.setCourse(result.getCourse());
			groupInfo.setFaculty(faculty);
			groupInfo.setLevel(result.getLevel());
			groupInfo.setSubgroups(result.getSubgroups());
			groupInfos.add(groupInfo);
		}
		return groupInfos;
	}

	@Override
	public List<String> getTeachersWithName(String teacherName) {
		return repository.getAllByTeacherNameStartsWith(teacherName.toLowerCase()).stream()
			.map(FindTeacherResult::getTeacherName)
			.collect(Collectors.toList());
	}

	@Override
	public List<String> getAllFaculties() {
		return repository.getAllFaculties();
	}

	@Override
	public List<LessonCell> getAllByGroup(String group, String faculty) {
		return repository.getAllByGroupAndFaculty(group, faculty);
	}

	@Override
	public Iterable<LessonCell> saveAll(Collection<LessonCell> collection) {
		return repository.saveAll(collection);
	}

	@Override
	public void deleteAll(Collection<LessonCell> collection) {
		repository.deleteAll(collection);
	}
}
