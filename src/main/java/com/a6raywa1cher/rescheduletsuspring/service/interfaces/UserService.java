package com.a6raywa1cher.rescheduletsuspring.service.interfaces;

import com.a6raywa1cher.rescheduletsuspring.models.User;

import java.util.Optional;

public interface UserService {
	Optional<User> getByUsername(String username);

	User create(String username, String password, boolean admin);

	User setAdmin(User user, boolean admin);

	User changePassword(User user, String password);

	void removeUser(User user);
}
