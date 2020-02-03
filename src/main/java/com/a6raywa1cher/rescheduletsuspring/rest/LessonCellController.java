package com.a6raywa1cher.rescheduletsuspring.rest;

import com.a6raywa1cher.rescheduletsuspring.components.tsudbimporter.ImportException;
import com.a6raywa1cher.rescheduletsuspring.components.tsudbimporter.TsuDbImporterComponent;
import com.a6raywa1cher.rescheduletsuspring.config.AppConfigProperties;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

@Controller
@CrossOrigin
@ApiResponses({
	@ApiResponse(code = 503, message = "DB is busy, try again after Retry-After seconds.")
})
public class LessonCellController {
	private static final Logger log = LoggerFactory.getLogger(TsuDbImporterComponent.class);
	private AppConfigProperties appConfigProperties;
	private TsuDbImporterComponent importer;

	@Autowired
	public LessonCellController(AppConfigProperties appConfigProperties, TsuDbImporterComponent importer) {
		this.appConfigProperties = appConfigProperties;
		this.importer = importer;
	}

	@PostMapping(path = "/force")
	@Transactional(rollbackOn = ImportException.class)
	@ApiOperation(value = "Force db import", notes = "Forces import from external db.")
	public ResponseEntity<?> forceUpdate(
		@RequestHeader(name = HttpHeaders.AUTHORIZATION) String authorization,
		@RequestParam(required = false, name = "override_cache", defaultValue = "false")
			Boolean overrideCache, HttpServletRequest request) throws ImportException {
		if (!appConfigProperties.getAdminToken().equals(authorization)) {
			log.warn("Attack attempt. ip:{}", request.getRemoteAddr());
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		try {
			importer.importExternalModels(overrideCache == null ? false : overrideCache);
		} catch (Exception e) {
			log.error(String.format("Error during forced update (flag %b)", overrideCache), e);
			throw e;
		}
		log.info("Imported external DB by request");
		return ResponseEntity.ok().build();
	}
}
