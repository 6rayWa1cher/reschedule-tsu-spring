package com.a6raywa1cher.rescheduletsuspring.security;

import com.a6raywa1cher.rescheduletsuspring.dao.repository.LessonCellRepository;
import com.a6raywa1cher.rescheduletsuspring.dao.repository.UserRepository;
import com.a6raywa1cher.rescheduletsuspring.models.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Optional;

@Service
@Qualifier("UserDetailsServiceImpl")
public class UserDetailsServiceImpl implements UserDetailsService {
	private UserRepository userRepository;
	private LessonCellRepository lessonCellRepository;

	public UserDetailsServiceImpl(UserRepository userRepository, LessonCellRepository lessonCellRepository) {
		this.userRepository = userRepository;
		this.lessonCellRepository = lessonCellRepository;
	}

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<User> optionalUser = userRepository.getByUsername(username);
		if (optionalUser.isEmpty()) {
			throw new UsernameNotFoundException(String.format("Username %s not found", username));
		}
		User user = optionalUser.get();
		return new DefaultUserDetails(user, lessonCellRepository.getAllByCreator(user), new ArrayList<>(user.getPermissions()));
	}
}