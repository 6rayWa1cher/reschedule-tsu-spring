package com.a6raywa1cher.rescheduletsuspring.rest.mirror;

import com.a6raywa1cher.rescheduletsuspring.models.User;
import lombok.Data;

@Data
public class UserMirror {
	private Long id;

	private String username;

	private boolean admin;

	public static UserMirror convert(User user) {
		UserMirror mirror = new UserMirror();
		mirror.setId(user.getId());
		mirror.setUsername(user.getUsername());
		mirror.setAdmin(user.isAdmin());
		return mirror;
	}
}
