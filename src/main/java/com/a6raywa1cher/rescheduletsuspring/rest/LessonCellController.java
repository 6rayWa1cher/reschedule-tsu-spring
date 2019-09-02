package com.a6raywa1cher.rescheduletsuspring.rest;

import com.a6raywa1cher.rescheduletsuspring.components.TsuDbImporter;
import com.a6raywa1cher.rescheduletsuspring.dao.interfaces.LessonCellService;
import com.a6raywa1cher.rescheduletsuspring.dao.results.FindGroupsAndSubgroupsResult;
import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import com.a6raywa1cher.rescheduletsuspring.rest.mirror.LessonCellMirror;
import com.a6raywa1cher.rescheduletsuspring.rest.response.GetGroupsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class LessonCellController {
	private TsuDbImporter importer;
	private LessonCellService service;

	@Autowired
	public LessonCellController(TsuDbImporter importer, LessonCellService service) {
		this.importer = importer;
		this.service = service;
	}

	@GetMapping(path = "/force")
	@Transactional
	public ResponseEntity<?> forceUpdate() {
		importer.importExternalModels();
		return ResponseEntity.ok().build();
	}

	@GetMapping(path = "/groups")
	public ResponseEntity<GetGroupsResponse> getGroupsList() {
		List<FindGroupsAndSubgroupsResult> results = service.findGroupsAndSubgroups();
		GetGroupsResponse response = new GetGroupsResponse();
		response.setGroups(new ArrayList<>());
		for (FindGroupsAndSubgroupsResult result : results) {
			response.getGroups().add(new GetGroupsResponse.GroupInfo(result.getGroup(), result.getSubgroups()));
		}
		return ResponseEntity.ok(response);
	}

	@GetMapping(path = "/group/{groupId}")
	public ResponseEntity<List<LessonCellMirror>> getSchedule(@PathVariable String groupId) {
		List<LessonCell> cells = service.getAllByGroup(groupId);
		return ResponseEntity.ok(cells.stream().map(LessonCellMirror::convert).collect(Collectors.toList()));
	}
}
