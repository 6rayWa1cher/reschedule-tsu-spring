package com.a6raywa1cher.rescheduletsuspring.service.impl;

import com.a6raywa1cher.rescheduletsuspring.dao.repository.UserRepository;
import com.a6raywa1cher.rescheduletsuspring.models.User;
import com.a6raywa1cher.rescheduletsuspring.service.interfaces.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;

	@Autowired
	public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public Optional<User> getByUsername(String username) {
		return userRepository.getByUsername(username);
	}

	@Override
	public User create(String username, String password, boolean admin) {
		User user = new User();
		user.setUsername(username);
		user.setPassword(passwordEncoder.encode(password));
		user.setAdmin(admin);
		return userRepository.save(user);
	}

	@Override
	public User setAdmin(User user, boolean admin) {
		user.setAdmin(admin);
		return userRepository.save(user);
	}

	@Override
	public User changePassword(User user, String password) {
		user.setPassword(passwordEncoder.encode(password));
		return userRepository.save(user);
	}

	@Override
	public void removeUser(User user) {
		userRepository.delete(user);
	}
}
