package com.a6raywa1cher.rescheduletsuspring.rest;

import com.a6raywa1cher.rescheduletsuspring.components.tsudbimporter.ImportException;
import com.a6raywa1cher.rescheduletsuspring.components.tsudbimporter.TsuDbImporterComponent;
import com.a6raywa1cher.rescheduletsuspring.components.weeksign.WeekSignComponent;
import com.a6raywa1cher.rescheduletsuspring.config.AppConfigProperties;
import com.a6raywa1cher.rescheduletsuspring.dao.interfaces.LessonCellService;
import com.a6raywa1cher.rescheduletsuspring.dao.results.FindGroupsAndSubgroupsResult;
import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import com.a6raywa1cher.rescheduletsuspring.models.WeekSign;
import com.a6raywa1cher.rescheduletsuspring.rest.mirror.LessonCellMirror;
import com.a6raywa1cher.rescheduletsuspring.rest.response.GetFacultiesResponse;
import com.a6raywa1cher.rescheduletsuspring.rest.response.GetGroupsResponse;
import com.a6raywa1cher.rescheduletsuspring.rest.response.GetScheduleForWeekResponse;
import com.a6raywa1cher.rescheduletsuspring.rest.response.GetWeekSignResponse;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@CrossOrigin
@ApiResponses({
	@ApiResponse(code = 503, message = "DB is busy, try again after Retry-After seconds.")
})
public class LessonCellController {
	private static final Logger log = LoggerFactory.getLogger(TsuDbImporterComponent.class);
	private AppConfigProperties appConfigProperties;
	private TsuDbImporterComponent importer;
	private LessonCellService service;
	private WeekSignComponent weekSignComponent;

	@Autowired
	public LessonCellController(AppConfigProperties appConfigProperties, TsuDbImporterComponent importer,
	                            LessonCellService service, WeekSignComponent weekSignComponent) {
		this.appConfigProperties = appConfigProperties;
		this.importer = importer;
		this.service = service;
		this.weekSignComponent = weekSignComponent;
	}

	@GetMapping(path = "/force")
	@Transactional(rollbackOn = ImportException.class)
	@ApiOperation(value = "Force db import", notes = "Forces import from external db.")
	public ResponseEntity<?> forceUpdate(
		@RequestHeader(name = HttpHeaders.AUTHORIZATION) String authorization,
		@RequestParam(required = false, name = "override_cache", defaultValue = "false")
			Boolean overrideCache) {
		if (!appConfigProperties.getAdminToken().equals(authorization)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		try {
			importer.importExternalModels(overrideCache == null ? false : overrideCache);
		} catch (ImportException e) {
			log.error(String.format("Error during forced update (flag %b)", overrideCache), e);
		}
		return ResponseEntity.ok().build();
	}

	@GetMapping(path = "/faculties")
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
		List<FindGroupsAndSubgroupsResult> results = service.findGroupsAndSubgroups(facultyId);
		GetGroupsResponse response = new GetGroupsResponse();
		response.setGroups(new ArrayList<>());
		for (FindGroupsAndSubgroupsResult result : results) {
			response.getGroups().add(new GetGroupsResponse.GroupInfo(
				result.getLevel().getDeserializationName(),
				result.getGroup(),
				result.getSubgroups(), result.getCourse(),
				fullTable != null && fullTable ? service.getAllByGroup(result.getGroup(), facultyId) : null
			));
		}
		return ResponseEntity.ok(response);
	}

	@GetMapping(path = "/{facultyId}/group/{groupId}")
	@ApiOperation(value = "Get raw schedule of group", notes = "Provides list of groups and additional info about them.")
	public ResponseEntity<List<LessonCellMirror>> getSchedule(@PathVariable String groupId, @PathVariable String facultyId) {
		List<LessonCell> cells = service.getAllByGroup(groupId, facultyId);
		return ResponseEntity.ok(cells.stream().map(LessonCellMirror::convert).collect(Collectors.toList()));
	}

	@GetMapping(path = "/{facultyId}/group/{groupId}/week")
	@ApiOperation(value = "Get ready-to-go schedule for 7 days",
		notes = "Provides schedule of certain group for 7 working days.")
	public ResponseEntity<GetScheduleForWeekResponse> getScheduleForWeek(
		@PathVariable String facultyId, @PathVariable String groupId,
		@ApiParam(value = "ISO Date Format, yyyy-MM-dd", example = "2019-12-28")
		@RequestParam(name = "day", required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		@Valid Date date) {
		List<LessonCell> allCells = service.getAllByGroup(groupId, facultyId);
		date = date == null ? new Date() : date;
		Map<Pair<WeekSign, DayOfWeek>, List<LessonCellMirror>> map = new LinkedHashMap<>();
		List<Pair<WeekSign, DayOfWeek>> daysList = new ArrayList<>();
		WeekSign currentWeekSign = weekSignComponent.getWeekSign(date, facultyId);
		DayOfWeek currentDayOfWeek = LocalDate.ofInstant(date.toInstant(), ZoneId.systemDefault()).getDayOfWeek();
		while (daysList.size() != 7) {
			if (currentDayOfWeek == DayOfWeek.SUNDAY) {
				currentDayOfWeek = DayOfWeek.MONDAY;
				currentWeekSign = WeekSign.inverse(currentWeekSign);
				continue;
			}
			log.info("{}, {}", currentWeekSign, currentDayOfWeek);
			map.put(Pair.of(currentWeekSign, currentDayOfWeek), new ArrayList<>());
			daysList.add(Pair.of(currentWeekSign, currentDayOfWeek));
			currentDayOfWeek = DayOfWeek.values()[currentDayOfWeek.ordinal() + 1];
		}
		for (LessonCell cell : allCells) {
			for (Pair<WeekSign, DayOfWeek> pair : daysList) {
				if (!cell.getDayOfWeek().equals(pair.getSecond())) continue;
				if (cell.getWeekSign() == WeekSign.ANY || pair.getFirst().equals(cell.getWeekSign())) {
					map.get(pair).add(LessonCellMirror.convert(cell));
				}
			}
		}
		GetScheduleForWeekResponse response = new GetScheduleForWeekResponse();
		for (Map.Entry<Pair<WeekSign, DayOfWeek>, List<LessonCellMirror>> entry : map.entrySet()) {
			GetScheduleForWeekResponse.Schedule schedule = new GetScheduleForWeekResponse.Schedule();
			schedule.setSign(entry.getKey().getFirst());
			schedule.setDayOfWeek(entry.getKey().getSecond());
			schedule.setCells(entry.getValue());
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
		GetWeekSignResponse response = new GetWeekSignResponse();
		response.setWeekSign(weekSignComponent.getWeekSign(date == null ? new Date() : date, facultyId));
		return ResponseEntity.ok(response);
	}
}
