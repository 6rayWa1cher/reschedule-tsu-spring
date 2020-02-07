package com.a6raywa1cher.rescheduletsuspring.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class MvcAccessChecker {
	public boolean checkFacultyAndGroup(Authentication authentication, String faculty, String group) {
		if (faculty == null || group == null) return false;
		if (faculty.equals("any")) return false;
		if (!faculty.matches("[а-яА-Яa-zA-Z, \\-0-9()]{3,50}") || !group.matches("[а-яА-Я, \\-0-9'.(М)]{1,150}")) {
			return false;
		}
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		try {
			return authorities.contains(new FacultyAndGroupGrantedAuthority("any", "any")) ||
				authorities.contains(new FacultyAndGroupGrantedAuthority(faculty, group));
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	public boolean checkUserLessonCell(Authentication authentication, String id) {
		if (id == null) return false;
		if (!id.matches("[a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8}")) return false;
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		if (authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) return true;
		return authorities.contains(new LessonCellGrantedAuthority(id));
	}
}
