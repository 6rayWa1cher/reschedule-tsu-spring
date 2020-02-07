package com.a6raywa1cher.rescheduletsuspring.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.Objects;

public class FacultyAndGroupGrantedAuthority implements GrantedAuthority {
	private final String faculty;

	private final String group;

	public FacultyAndGroupGrantedAuthority(String faculty, String group) {
		this.faculty = faculty;
		this.group = group;
	}

	@Override
	public String getAuthority() {
		return "FG_" + faculty + "#" + group;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FacultyAndGroupGrantedAuthority that = (FacultyAndGroupGrantedAuthority) o;
		return faculty.equals(that.faculty) &&
			group.equals(that.group);
	}

	@Override
	public int hashCode() {
		return Objects.hash(faculty, group);
	}

	@Override
	public String toString() {
		return "FacultyAndGroupGrantedAuthority{" +
			"faculty='" + faculty + '\'' +
			", group='" + group + '\'' +
			'}';
	}
}
