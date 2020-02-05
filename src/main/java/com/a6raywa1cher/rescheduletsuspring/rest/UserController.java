package com.a6raywa1cher.rescheduletsuspring.rest;

import com.a6raywa1cher.rescheduletsuspring.models.User;
import com.a6raywa1cher.rescheduletsuspring.rest.mirror.UserMirror;
import com.a6raywa1cher.rescheduletsuspring.rest.mirror.View;
import com.a6raywa1cher.rescheduletsuspring.rest.request.ChangePasswordRequest;
import com.a6raywa1cher.rescheduletsuspring.rest.request.CreateUserRequest;
import com.a6raywa1cher.rescheduletsuspring.rest.request.DeleteUserRequest;
import com.a6raywa1cher.rescheduletsuspring.service.interfaces.UserService;
import com.fasterxml.jackson.annotation.JsonView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

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

	@PostMapping("/reg")
	public ResponseEntity<UserMirror> createUser(@RequestBody @Valid CreateUserRequest dto) {
		if (userService.getByUsername(dto.getUsername()).isPresent()) {
			return ResponseEntity.badRequest().build();
		}
		return ResponseEntity.ok(UserMirror.convert(userService.create(dto.getUsername(), dto.getPassword(), false)));
	}

	@PostMapping("/change_password")
	public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordRequest dto) {
		UserDetails userDetails = getUserDetails();
		User user = userService.getByUsername(userDetails.getUsername()).orElseThrow();
		userService.changePassword(user, dto.getPassword());
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/delete_user")
	public ResponseEntity<Void> deleteUser(@RequestBody @Valid DeleteUserRequest dto) {
		Optional<User> user = userService.getByUsername(dto.getUsername());
		if (user.isEmpty()) {
			return ResponseEntity.badRequest().build();
		}
		userService.removeUser(user.get());
		return ResponseEntity.ok().build();
	}
}
