package com.a6raywa1cher.rescheduletsuspring.rest;

import com.a6raywa1cher.rescheduletsuspring.components.tsudbimporter.ImportException;
import com.a6raywa1cher.rescheduletsuspring.components.tsudbimporter.TsuDbImporterComponent;
import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import com.a6raywa1cher.rescheduletsuspring.models.User;
import com.a6raywa1cher.rescheduletsuspring.models.submodels.LessonCellCoordinates;
import com.a6raywa1cher.rescheduletsuspring.rest.mirror.LessonCellMirror;
import com.a6raywa1cher.rescheduletsuspring.rest.mirror.View;
import com.a6raywa1cher.rescheduletsuspring.rest.request.CreateLessonCellRequest;
import com.a6raywa1cher.rescheduletsuspring.rest.request.DeleteLessonCellRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
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
	private TsuDbImporterComponent importer;
	private UserService userService;
	private LessonCellService lessonCellService;

	@Autowired
	public LessonCellController(TsuDbImporterComponent importer, UserService userService, LessonCellService lessonCellService) {
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

	@PostMapping(path = "/force")
	@Transactional(rollbackOn = ImportException.class)
	@ApiOperation(value = "Force db import", notes = "Forces import from external db.")
	public ResponseEntity<?> forceUpdate(
		@RequestParam(required = false, name = "override_cache", defaultValue = "false")
			Boolean overrideCache) throws ImportException {
		try {
			importer.importExternalModels(overrideCache == null ? false : overrideCache);
		} catch (Exception e) {
			log.error(String.format("Error during forced update (flag %b)", overrideCache), e);
			throw e;
		}
		log.info("Imported external DB by request");
		return ResponseEntity.ok().build();
	}

	@PostMapping("/add")
	@Transactional(rollbackOn = Exception.class)
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

	@GetMapping("/u/{username:[a-zA-Z0-9]{3,35}}/cells")
	public ResponseEntity<Page<LessonCellMirror>> getByUser(@PathVariable @Valid String username, Pageable pageable) {
		Optional<User> optionalUser = userService.getByUsername(username);
		if (optionalUser.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		Page<LessonCell> page = lessonCellService.getByUser(optionalUser.get(), pageable);
		Page<LessonCellMirror> output = new PageImpl<>(
			page.getContent().stream()
				.map(LessonCellMirror::convert)
				.collect(Collectors.toList()),
			page.getPageable(),
			page.getTotalElements()
		);
		return ResponseEntity.ok(output);
	}


	@DeleteMapping("/delete")
	public ResponseEntity<?> deleteOwnCell(@RequestBody @Valid DeleteLessonCellRequest dto) {
		User user = getUser();
		Optional<LessonCell> optional = lessonCellService.getById(dto.getId());
		if (optional.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		LessonCell cell = optional.get();
		if (cell.getCreator() == null) {
			return ResponseEntity.badRequest().body("Can't delete a non-user cell");
		}
		if (!user.getId().equals(cell.getCreator().getId())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body("Can't delete other user's LessionCell with this command");
		}
		lessonCellService.deleteAll(Collections.singleton(cell));
		log.info("User {} deleted a LessonCell {}", user.getUsername(), cell.toString());
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/delete_sudo")
	public ResponseEntity<?> deleteCell(@RequestBody @Valid DeleteLessonCellRequest dto) {
		User user = getUser();
		Optional<LessonCell> optional = lessonCellService.getById(dto.getId());
		if (optional.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		LessonCell cell = optional.get();
		if (cell.getCreator() == null) {
			return ResponseEntity.badRequest().body("Can't delete a non-user cell");
		}
		lessonCellService.deleteAll(Collections.singleton(cell));
		log.info("User {} deleted with force a LessonCell {}", user.getUsername(), cell.toString());
		return ResponseEntity.ok().build();
	}
}
