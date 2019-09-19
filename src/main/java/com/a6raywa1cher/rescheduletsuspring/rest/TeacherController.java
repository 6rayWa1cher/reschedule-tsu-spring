package com.a6raywa1cher.rescheduletsuspring.rest;

import com.a6raywa1cher.rescheduletsuspring.rest.mirror.LessonCellMirror;
import com.a6raywa1cher.rescheduletsuspring.rest.response.GetScheduleOfTeacherForWeekResponse;
import com.a6raywa1cher.rescheduletsuspring.rest.response.GetScheduleOfTeacherResponse;
import com.a6raywa1cher.rescheduletsuspring.rest.response.GetTeachersResponse;
import com.a6raywa1cher.rescheduletsuspring.service.interfaces.LessonCellService;
import com.a6raywa1cher.rescheduletsuspring.service.submodels.DaySchedule;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.stream.Collectors;

@Controller
@CrossOrigin
@RequestMapping("/teachers")
public class TeacherController {
	private static final Logger log = LoggerFactory.getLogger(TeacherController.class);
	private LessonCellService service;

	@Autowired
	public TeacherController(LessonCellService service) {
		this.service = service;
	}

	@GetMapping("/find/{teacherName}")
	public ResponseEntity<GetTeachersResponse> getTeachersByName(@PathVariable String teacherName) {
		GetTeachersResponse response = new GetTeachersResponse();
		response.setTeachers(service.getTeachersWithName(teacherName));
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{teacherName}")
	public ResponseEntity<GetScheduleOfTeacherResponse> getRawSchedule(@PathVariable String teacherName) {
		GetScheduleOfTeacherResponse response = new GetScheduleOfTeacherResponse();
		response.setRawSchedule(service.getTeacherRaw(teacherName).stream()
			.map(LessonCellMirror::convert)
			.collect(Collectors.toList()));
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{teacherName}/week")
	public ResponseEntity<GetScheduleOfTeacherForWeekResponse> getScheduleOfTeacherForWeek(
		@PathVariable String teacherName,
		@ApiParam(value = "ISO Date Format, yyyy-MM-dd", example = "2019-12-28")
		@RequestParam(name = "day", required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		@Valid Date date) {
		date = date == null ? new Date() : date;
		GetScheduleOfTeacherForWeekResponse response = new GetScheduleOfTeacherForWeekResponse();
		for (DaySchedule daySchedule : service.getReadyTeacherSchedules(teacherName, date, 7)) {
			GetScheduleOfTeacherForWeekResponse.Schedule schedule = new GetScheduleOfTeacherForWeekResponse.Schedule();
			schedule.setSign(daySchedule.getSign());
			schedule.setDayOfWeek(daySchedule.getDayOfWeek());
			schedule.setCells(daySchedule.getCells().stream().map(LessonCellMirror::convert).collect(Collectors.toList()));
			response.getSchedules().add(schedule);
		}
		return ResponseEntity.ok(response);
	}
}
