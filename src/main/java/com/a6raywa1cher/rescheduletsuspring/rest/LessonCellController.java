package com.a6raywa1cher.rescheduletsuspring.rest;

import com.a6raywa1cher.rescheduletsuspring.components.TsuDbImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.transaction.Transactional;

@Controller
public class LessonCellController {
	private TsuDbImporter importer;

	@Autowired
	public LessonCellController(TsuDbImporter importer) {
		this.importer = importer;
	}

	@GetMapping(path = "/force")
	@Transactional
	public ResponseEntity<?> forceUpdate() {
		importer.importExternalModels();
		return ResponseEntity.ok().build();
	}
}
