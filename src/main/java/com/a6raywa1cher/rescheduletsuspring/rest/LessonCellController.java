package com.a6raywa1cher.rescheduletsuspring.rest;

import com.a6raywa1cher.rescheduletsuspring.components.TsuDbImporter;
import com.a6raywa1cher.rescheduletsuspring.config.AppConfig;
import com.a6raywa1cher.rescheduletsuspring.dao.interfaces.LessonCellService;
import com.a6raywa1cher.rescheduletsuspring.dao.results.FindGroupsAndSubgroupsResult;
import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import com.a6raywa1cher.rescheduletsuspring.rest.mirror.LessonCellMirror;
import com.a6raywa1cher.rescheduletsuspring.rest.response.GetFacultiesResponse;
import com.a6raywa1cher.rescheduletsuspring.rest.response.GetGroupsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class LessonCellController {
	private AppConfig appConfig;
	private TsuDbImporter importer;
	private LessonCellService service;

	@Autowired
	public LessonCellController(AppConfig appConfig, TsuDbImporter importer, LessonCellService service) {
		this.appConfig = appConfig;
		this.importer = importer;
		this.service = service;
	}

	@GetMapping(path = "/force")
	@Transactional
	public ResponseEntity<?> forceUpdate(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION) String authorization,
			@RequestParam(required = false, name = "override_cache", defaultValue = "false")
					Boolean overrideCache) {
		if (!appConfig.getAdminToken().equals(authorization)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		importer.importExternalModels(overrideCache == null ? false : overrideCache);
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
	public ResponseEntity<GetGroupsResponse> getGroupsList(@PathVariable String facultyId) {
		List<FindGroupsAndSubgroupsResult> results = service.findGroupsAndSubgroups(facultyId);
		GetGroupsResponse response = new GetGroupsResponse();
		response.setGroups(new ArrayList<>());
		for (FindGroupsAndSubgroupsResult result : results) {
			response.getGroups().add(new GetGroupsResponse.GroupInfo(result.getGroup(), result.getSubgroups()));
		}
		return ResponseEntity.ok(response);
	}

	@GetMapping(path = "/{facultyId}/group/{groupId}")
	public ResponseEntity<List<LessonCellMirror>> getSchedule(@PathVariable String groupId, @PathVariable String facultyId) {
		List<LessonCell> cells = service.getAllByGroup(groupId, facultyId);
		return ResponseEntity.ok(cells.stream().map(LessonCellMirror::convert).collect(Collectors.toList()));
	}
}
