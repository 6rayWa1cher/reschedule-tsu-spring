package com.a6raywa1cher.rescheduletsuspring.rest;

import com.a6raywa1cher.rescheduletsuspring.components.tsudbimporter.ImportException;
import com.a6raywa1cher.rescheduletsuspring.components.tsudbimporter.TsuDbImporterComponent;
import com.a6raywa1cher.rescheduletsuspring.components.weeksign.WeekSignComponent;
import com.a6raywa1cher.rescheduletsuspring.config.AppConfigProperties;
import com.a6raywa1cher.rescheduletsuspring.dao.interfaces.LessonCellService;
import com.a6raywa1cher.rescheduletsuspring.dao.results.FindGroupsAndSubgroupsResult;
import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import com.a6raywa1cher.rescheduletsuspring.rest.mirror.LessonCellMirror;
import com.a6raywa1cher.rescheduletsuspring.rest.response.GetFacultiesResponse;
import com.a6raywa1cher.rescheduletsuspring.rest.response.GetGroupsResponse;
import com.a6raywa1cher.rescheduletsuspring.rest.response.GetWeekSignResponse;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@CrossOrigin
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
	public ResponseEntity<GetFacultiesResponse> getFaculties() {
		List<String> faculties = service.getAllFaculties();
		GetFacultiesResponse response = new GetFacultiesResponse();
		response.setFaculties(faculties);
		return ResponseEntity.ok(response);
	}

	@GetMapping(path = "/{facultyId}/groups")
	public ResponseEntity<GetGroupsResponse> getGroupsList(
		@PathVariable String facultyId,
		@ApiParam(value = "Include LessonCells (use with caution!)")
		@RequestParam(name = "full_table", required = false, defaultValue = "false")
			Boolean fullTable) {
		List<FindGroupsAndSubgroupsResult> results = service.findGroupsAndSubgroups(facultyId);
		GetGroupsResponse response = new GetGroupsResponse();
		response.setGroups(new ArrayList<>());
		for (FindGroupsAndSubgroupsResult result : results) {
			response.getGroups().add(new GetGroupsResponse.GroupInfo(result.getGroup(),
				result.getSubgroups(), result.getCourse(),
				fullTable != null && fullTable ? service.getAllByGroup(result.getGroup(), facultyId) : null));
		}
		return ResponseEntity.ok(response);
	}

	@GetMapping(path = "/{facultyId}/group/{groupId}")
	public ResponseEntity<List<LessonCellMirror>> getSchedule(@PathVariable String groupId, @PathVariable String facultyId) {
		List<LessonCell> cells = service.getAllByGroup(groupId, facultyId);
		return ResponseEntity.ok(cells.stream().map(LessonCellMirror::convert).collect(Collectors.toList()));
	}

	@GetMapping(path = "/{facultyId}/week_sign")
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
