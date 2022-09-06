package com.a6raywa1cher.rescheduletsuspring.rest;

import com.a6raywa1cher.rescheduletsuspring.components.tsudbimporter.ImportException;
import com.a6raywa1cher.rescheduletsuspring.components.tsudbimporter.TsuDbImporterComponent;
import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import com.a6raywa1cher.rescheduletsuspring.models.User;
import com.a6raywa1cher.rescheduletsuspring.models.submodels.LessonCellCoordinates;
import com.a6raywa1cher.rescheduletsuspring.rest.mirror.LessonCellMirror;
import com.a6raywa1cher.rescheduletsuspring.rest.mirror.View;
import com.a6raywa1cher.rescheduletsuspring.rest.request.CreateLessonCellRequest;
import com.a6raywa1cher.rescheduletsuspring.security.DefaultUserDetails;
import com.a6raywa1cher.rescheduletsuspring.service.interfaces.LessonCellService;
import com.a6raywa1cher.rescheduletsuspring.service.interfaces.UserService;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@CrossOrigin
@ApiResponses({
	@ApiResponse(code = 503, message = "DB is busy, try again after Retry-After seconds.")
})
@RequestMapping("/cells")
@JsonView({View.Public.class, View.Internal.class})
public class LessonCellController {
	private static final Logger log = LoggerFactory.getLogger(LessonCellController.class);
	private final TsuDbImporterComponent importer;
	private final UserService userService;
	private final LessonCellService lessonCellService;

	@Autowired
	public LessonCellController(
		@Autowired(required = false) TsuDbImporterComponent importer, UserService userService, LessonCellService lessonCellService) {
		this.importer = importer;
		this.userService = userService;
		this.lessonCellService = lessonCellService;
	}

	private User getUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication.getPrincipal() instanceof DefaultUserDetails) {
			return ((DefaultUserDetails) authentication.getPrincipal()).getUser();
		} else {
			return userService.getByUsername(authentication.getName()).orElseThrow();
		}
	}

	@PostMapping(path = "/force_update")
	@Transactional(rollbackOn = ImportException.class)
	@ApiOperation(value = "Force db import", notes = "Forces import from external db.")
	public ResponseEntity<?> forceUpdate(
		@RequestParam(required = false, name = "override_cache", defaultValue = "false")
		Boolean overrideCache) throws ImportException {
		if (importer != null) {
			try {
				importer.importExternalModels(overrideCache == null ? false : overrideCache);
			} catch (Exception e) {
				log.error(String.format("Error during forced update (flag %b)", overrideCache), e);
				throw e;
			}
			log.info("Imported external DB by request");
		} else {
			log.info("Skipped force update request: importer is disabled");
		}
		return ResponseEntity.ok().build();
	}

	@PostMapping("/add")
	@Transactional(rollbackOn = Exception.class)
	@PreAuthorize("@mvcAccessChecker.checkFacultyAndGroup(authentication,#dto.getFaculty(),#dto.getGroup())")
	@ApiOperation(
		value = "Add a LessonCell",
		notes = "Create a new user-made LessonCell.\n\n" +
			"Users can create LessonCells only according to their permissions " +
			"(&lt;faculty&gt;#&lt;group&gt; must be in permissions list).\n" +
			"Admins can create LessonsCells without faculty-group limitation."
	)
	public ResponseEntity<?> addCell(@RequestBody @Valid CreateLessonCellRequest dto) {
		User user = getUser();
		List<LessonCell> list = lessonCellService.getByLessonCellCoordinates(new LessonCellCoordinates(
			dto.getFaculty(), dto.getGroup(), dto.getSubgroup(), dto.getWeekSign(),
			dto.getDayOfWeek(), dto.getColumnPosition(), dto.getTeacherName()
		)).collect(Collectors.toList());
		if (!list.isEmpty()) {
			if (list.stream().anyMatch(lc -> Objects.nonNull(lc.getCreator()))) {
				return ResponseEntity.badRequest().body("One of LessonCell created by another user");
			}
			if (!dto.getIgnoreExternalDb() && !dto.getIgnoreLastExternalDbRecord()) {
				return ResponseEntity.badRequest().body(
					"LessonCells exists, but ignoreExternalDb and ignoreLastExternalDbRecord is false"
				);
			}
		}
		LessonCell lessonCell = new LessonCell();
		lessonCell.transfer(dto);
		lessonCell.setCreator(user);
		LessonCell addedCell = lessonCellService.addUserCell(lessonCell, dto.getIgnoreLastExternalDbRecord());
		log.info("User {} added a LessonCell {}", user.getUsername(), lessonCell.toString());
		return ResponseEntity.ok(LessonCellMirror.convert(addedCell));
	}

	@GetMapping("/u/{username}/cells")
	@ApiOperation(value = "Get LessonCells by creator")
	public ResponseEntity<Page<LessonCellMirror>> getByUser(@PathVariable @Valid String username,
	                                                        @RequestParam(required = false) String faculty,
	                                                        @RequestParam(required = false) String group,
	                                                        Pageable pageable) {
		Optional<User> optionalUser = userService.getByUsername(username);
		if (optionalUser.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		Page<LessonCell> page;
		if (StringUtils.isEmpty(faculty) && StringUtils.isEmpty(group)) {
			page = lessonCellService.getByUser(optionalUser.get(), pageable);
		} else if (StringUtils.isEmpty(group)) {
			page = lessonCellService.getByUserFaculty(optionalUser.get(), faculty, pageable);
		} else {
			page = lessonCellService.getByUserFacultyGroup(optionalUser.get(), faculty, group, pageable);
		}
		Page<LessonCellMirror> output = new PageImpl<>(
			page.getContent().stream()
				.map(LessonCellMirror::convert)
				.collect(Collectors.toList()),
			page.getPageable(),
			page.getTotalElements()
		);
		return ResponseEntity.ok(output);
	}

	@GetMapping("/c/{id}")
	@ApiOperation(
		value = "Get the LessonCell by id",
		notes = "Retrive the LessonCell (no matter, user-made or external) by id."
	)
	public ResponseEntity<LessonCellMirror> getCell(@PathVariable String id) {
		Optional<LessonCell> optional = lessonCellService.getById(id);
		if (optional.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(LessonCellMirror.convert(optional.get()));
	}

	@DeleteMapping("/c/{id}")
	@PreAuthorize("@mvcAccessChecker.checkUserLessonCell(authentication,#id)")
	@ApiOperation(
		value = "Delete LessonCell",
		notes = "Delete the LessonCell (user-made only) by id.\n\n" +
			"Users can delete only their own LessonCells.\n" +
			"Admins can delete any user-made LessonCell."
	)
	public ResponseEntity<?> deleteCell(@PathVariable @Valid String id) {
		User user = getUser();
		Optional<LessonCell> optional = lessonCellService.getById(id);
		if (optional.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		LessonCell cell = optional.get();
		if (cell.getCreator() == null) {
			return ResponseEntity.badRequest().body("Can't delete a non-user cell");
		}
		lessonCellService.deleteAll(Collections.singleton(cell));
		log.info("User {} deleted a LessonCell {}", user.getUsername(), cell.toString());
		return ResponseEntity.ok().build();
	}
}
