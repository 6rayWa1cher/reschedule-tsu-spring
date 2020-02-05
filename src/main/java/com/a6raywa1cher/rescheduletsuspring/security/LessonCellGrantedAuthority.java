package com.a6raywa1cher.rescheduletsuspring.security;

import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import org.springframework.security.core.GrantedAuthority;

import java.util.Objects;

public class LessonCellGrantedAuthority implements GrantedAuthority {
	private final String id;

	public LessonCellGrantedAuthority(LessonCell lc) {
		this.id = lc.getExternalId();
	}

	public LessonCellGrantedAuthority(String id) {
		this.id = id;
	}

	@Override
	public String getAuthority() {
		return "LC_" + id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LessonCellGrantedAuthority that = (LessonCellGrantedAuthority) o;
		return id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return getAuthority();
	}
}
