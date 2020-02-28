package com.a6raywa1cher.rescheduletsuspring.rest;

import com.a6raywa1cher.rescheduletsuspring.components.weeksign.WeekSignComponent;
import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import com.a6raywa1cher.rescheduletsuspring.rest.mirror.LessonCellMirror;
import com.a6raywa1cher.rescheduletsuspring.rest.mirror.View;
import com.a6raywa1cher.rescheduletsuspring.rest.response.GetFacultiesResponse;
import com.a6raywa1cher.rescheduletsuspring.rest.response.GetGroupsResponse;
import com.a6raywa1cher.rescheduletsuspring.rest.response.GetScheduleForWeekResponse;
import com.a6raywa1cher.rescheduletsuspring.rest.response.GetWeekSignResponse;
import com.a6raywa1cher.rescheduletsuspring.service.interfaces.LessonCellService;
import com.a6raywa1cher.rescheduletsuspring.service.submodels.DaySchedule;
import com.a6raywa1cher.rescheduletsuspring.service.submodels.GroupInfo;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@CrossOrigin
@RequestMapping("/faculties")
public class FacultyController {
	private static final Logger log = LoggerFactory.getLogger(FacultyController.class);
	private LessonCellService service;
	private WeekSignComponent weekSignComponent;

	@Autowired
	public FacultyController(LessonCellService service, WeekSignComponent weekSignComponent) {
		this.service = service;
		this.weekSignComponent = weekSignComponent;
	}

	@GetMapping(path = "")
	@ApiOperation(value = "Get all faculties", notes = "Provides list of facultyId, human-readable.")
	public ResponseEntity<GetFacultiesResponse> getFaculties() {
		List<String> faculties = service.getAllFaculties();
		GetFacultiesResponse response = new GetFacultiesResponse();
		response.setFaculties(faculties);
		return ResponseEntity.ok(response);
	}

	@GetMapping(path = "/{facultyId}/groups")
	@ApiOperation(value = "Get all groups of faculty", notes = "Provides list of groups and additional info about them.")
	public ResponseEntity<GetGroupsResponse> getGroupsList(
		@PathVariable String facultyId,
		@ApiParam(value = "Include LessonCells (use with caution!)")
		@RequestParam(name = "full_table", required = false, defaultValue = "false")
			Boolean fullTable) {
		if (!service.isFacultyExists(facultyId)) {
			return ResponseEntity.notFound().build();
		}
		List<GroupInfo> results = service.findGroupsAndSubgroups(facultyId);
		GetGroupsResponse response = new GetGroupsResponse();
		response.setGroups(new ArrayList<>());
		for (GroupInfo result : results) {
			response.getGroups().add(new GetGroupsResponse.GroupInfo(
				result.getLevel().getDeserializationName(),
				result.getName(),
				result.getSubgroups(), result.getCourse(),
				fullTable != null && fullTable ? service.getAllByGroup(result.getName(), facultyId)
					.stream().map(LessonCellMirror::convert).collect(Collectors.toList()) : null
			));
		}
		return ResponseEntity.ok(response);
	}

	@GetMapping(path = "/{facultyId}/groups/{groupId:.+}")
	@ApiOperation(value = "Get raw schedule of group", notes = "Provides list of groups and additional info about them.")
	@JsonView(View.Public.class)
	public ResponseEntity<List<LessonCellMirror>> getSchedule(@PathVariable String groupId, @PathVariable String facultyId) {
		if (!service.isGroupExists(facultyId, groupId)) {
			return ResponseEntity.notFound().build();
		}
		List<LessonCell> cells = service.getAllByGroup(groupId, facultyId);
		return ResponseEntity.ok(cells.stream().map(LessonCellMirror::convert).collect(Collectors.toList()));
	}

	@GetMapping(path = "/{facultyId}/groups/{groupId:.+}/week")
	@ApiOperation(value = "Get ready-to-go schedule for 7 days",
		notes = "Provides schedule of certain group for 7 working days.")
	@JsonView(View.Public.class)
	public ResponseEntity<GetScheduleForWeekResponse> getScheduleForWeek(
		@PathVariable String facultyId, @PathVariable String groupId,
		@ApiParam(value = "ISO Date Format, yyyy-MM-dd", example = "2019-12-28")
		@RequestParam(name = "day", required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		@Valid Date date) {
		if (!service.isGroupExists(facultyId, groupId)) {
			return ResponseEntity.notFound().build();
		}
		date = date == null ? new Date() : date;
		List<DaySchedule> daySchedules = service.getReadySchedules(facultyId, groupId, date, 7);
		GetScheduleForWeekResponse response = new GetScheduleForWeekResponse();
		for (DaySchedule daySchedule : daySchedules) {
			GetScheduleForWeekResponse.Schedule schedule = new GetScheduleForWeekResponse.Schedule();
			schedule.setSign(daySchedule.getSign());
			schedule.setDayOfWeek(daySchedule.getDayOfWeek());
			schedule.setCells(daySchedule.getCells().stream().map(LessonCellMirror::convert).collect(Collectors.toList()));
			response.getSchedules().add(schedule);
		}
		return ResponseEntity.ok(response);
	}

	@GetMapping(path = "/{facultyId}/week_sign")
	@ApiOperation(value = "Get week sign", notes = "Get week sign of certain faculty.")
	public ResponseEntity<GetWeekSignResponse> getWeekSign(
		@ApiParam(value = "ISO Date Format, yyyy-MM-dd", example = "2019-12-28")
		@RequestParam(name = "day", required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		@Valid
			Date date,
		@PathVariable String facultyId) {
		if (!service.isFacultyExists(facultyId)) {
			return ResponseEntity.notFound().build();
		}
		GetWeekSignResponse response = new GetWeekSignResponse();
		response.setWeekSign(weekSignComponent.getWeekSign(date == null ? new Date() : date, facultyId));
		return ResponseEntity.ok(response);
	}
}
