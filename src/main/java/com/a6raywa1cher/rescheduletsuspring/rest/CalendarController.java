package com.a6raywa1cher.rescheduletsuspring.rest;

import com.a6raywa1cher.rescheduletsuspring.models.Semester;
import com.a6raywa1cher.rescheduletsuspring.service.interfaces.CalendarService;
import com.a6raywa1cher.rescheduletsuspring.service.interfaces.LessonCellService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/calendar")
@CrossOrigin
@RequiredArgsConstructor
public class CalendarController {
	private final CalendarService calendarService;

	private final LessonCellService lessonCellService;

	@Value("${app.tsudb.semester}")
	private String semester;

	@Value("${app.tsudb.current-season}")
	private String currentSeason;

	private int getCurrentStudyYear() {
		return Integer.parseInt(currentSeason.split("-")[0].strip());
	}

	@GetMapping("/{facultyId}/{groupId}/calendar.ics")
	@ApiOperation(value = "Get calendar file for group")
	public ResponseEntity<String> getCalendarForGroup(
		@PathVariable String facultyId,
		@PathVariable String groupId
	) {
		if (!lessonCellService.isGroupExists(facultyId, groupId)) {
			return ResponseEntity.notFound().build();
		}

		String calendar = calendarService.getCalendarForGroup(
			facultyId, groupId, getCurrentStudyYear(), Semester.fromName(semester)
		);

		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.CONTENT_TYPE, "text/calendar; charset=UTF-8");
		headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=calendar.ics");

		return new ResponseEntity<>(calendar, headers, HttpStatus.OK);
	}
}
