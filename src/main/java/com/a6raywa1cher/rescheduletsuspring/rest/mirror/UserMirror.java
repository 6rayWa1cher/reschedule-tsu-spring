package com.a6raywa1cher.rescheduletsuspring.rest.mirror;

import com.a6raywa1cher.rescheduletsuspring.models.User;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserMirror {
	@JsonView(View.Public.class)
	private Long id;

	@JsonView(View.Public.class)
	private String username;

	@JsonView(View.Internal.class)
	private boolean admin;

	@JsonView(View.Internal.class)
	private List<String> permissions;

	public static UserMirror convert(User user) {
		UserMirror mirror = new UserMirror();
		mirror.setId(user.getId());
		mirror.setUsername(user.getUsername());
		mirror.setAdmin(user.isAdmin());
		if (user.getPermissions() != null) {
			mirror.setPermissions(new ArrayList<>(user.getPermissions()));
		} else {
			mirror.setPermissions(new ArrayList<>());
		}
		return mirror;
	}
}
