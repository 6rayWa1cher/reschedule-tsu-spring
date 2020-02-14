package com.a6raywa1cher.rescheduletsuspring.rest;

import com.a6raywa1cher.rescheduletsuspring.models.User;
import com.a6raywa1cher.rescheduletsuspring.rest.mirror.UserMirror;
import com.a6raywa1cher.rescheduletsuspring.rest.mirror.View;
import com.a6raywa1cher.rescheduletsuspring.rest.request.ChangePasswordRequest;
import com.a6raywa1cher.rescheduletsuspring.rest.request.CreateUserRequest;
import com.a6raywa1cher.rescheduletsuspring.rest.request.DeleteUserRequest;
import com.a6raywa1cher.rescheduletsuspring.rest.request.GrantPermissionRequest;
import com.a6raywa1cher.rescheduletsuspring.service.interfaces.UserService;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.util.Optional;

@Controller
@RequestMapping("/user")
@JsonView({View.Public.class, View.Internal.class})
public class UserController {
	private static final Logger log = LoggerFactory.getLogger(UserController.class);
	private UserService userService;

	@Autowired
	public UserController(UserService userService) {
		this.userService = userService;
	}

	private UserDetails getUserDetails() {
		return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	@PostConstruct
	public void postConstruct() {
		Optional<User> admin = userService.getByUsername("admin");
		User user = admin.orElse(null);
		if (admin.isEmpty()) {
			user = userService.create("admin", "admin", true);
			log.warn("Created admin user. Password: admin");
		}
		if (!user.isAdmin()) {
			userService.setAdmin(user, true);
			log.warn("Restored admin user permissions");
		}
	}

	@GetMapping("/cookies")
	@ApiOperation(
		value = "Check credentials",
		notes = "Safe endpoint for validating user credentials. Returns 200 in success, 4** otherwise."
	)
	public ResponseEntity<String> safeZone() {
		return ResponseEntity.ok("COOKIES!");
	}

	@PostMapping("/reg")
	@ApiOperation(
		value = "Register a new user",
		notes = "Registration form of new user. Only admin can create a new user."
	)
	public ResponseEntity<UserMirror> createUser(@RequestBody @Valid CreateUserRequest dto) {
		if (userService.getByUsername(dto.getUsername()).isPresent()) {
			return ResponseEntity.badRequest().build();
		}
		User user = userService.create(dto.getUsername(), dto.getPassword(), false);
		log.info("Created user {} by {}", user.getUsername(), getUserDetails().getUsername());
		return ResponseEntity.ok(UserMirror.convert(user));
	}

	@GetMapping("/u/{username}")
	@ApiOperation(
		value = "Get user info by username"
	)
	public ResponseEntity<UserMirror> getUser(@PathVariable String username) {
		Optional<User> optionalUser = userService.getByUsername(username);
		if (optionalUser.isEmpty()) return ResponseEntity.notFound().build();
		return ResponseEntity.ok(UserMirror.convert(optionalUser.get()));
	}

	@PostMapping("/grant")
	@ApiOperation(
		value = "Issue a new faculty-group permission",
		notes = "Issues a new record in other user's 'permissions' list. Format: &lt;faculty&gt;#&lt;group&gt;.\n" +
			"Only admins can issue permissions."
	)
	public ResponseEntity<UserMirror> grantPermission(@RequestBody @Valid GrantPermissionRequest dto) {
		Optional<User> optionalUser = userService.getByUsername(dto.getUsername());
		if (optionalUser.isEmpty()) return ResponseEntity.notFound().build();
		User out = userService.grantPermission(optionalUser.get(), dto.getFaculty(), dto.getGroup());
		log.info("User {} granted permission {}#{} to {}",
			getUserDetails().getUsername(), dto.getFaculty(), dto.getGroup(), dto.getUsername());
		return ResponseEntity.ok(UserMirror.convert(out));
	}

	@PostMapping("/change_password")
	@ApiOperation(
		value = "Change password",
		notes = "Changes user's password to another."
	)
	public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordRequest dto) {
		UserDetails userDetails = getUserDetails();
		User user = userService.getByUsername(userDetails.getUsername()).orElseThrow();
		userService.changePassword(user, dto.getPassword());
		log.info("Changed password of {}", user.getUsername());
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/delete_user")
	@ApiOperation(
		value = "Delete user",
		notes = "Deletes user. Only admins can delete users (including other admins)."
	)
	public ResponseEntity<Void> deleteUser(@RequestBody @Valid DeleteUserRequest dto) {
		Optional<User> user = userService.getByUsername(dto.getUsername());
		if (user.isEmpty()) {
			return ResponseEntity.badRequest().build();
		}
		userService.removeUser(user.get());
		log.info("User {} deleted user {}", getUserDetails().getUsername(), dto.getUsername());
		return ResponseEntity.ok().build();
	}
}
