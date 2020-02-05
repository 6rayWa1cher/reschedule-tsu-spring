package com.a6raywa1cher.rescheduletsuspring.security;

import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import com.a6raywa1cher.rescheduletsuspring.models.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultUserDetails implements UserDetails {
	private User user;
	private Set<GrantedAuthority> grantedAuthorities;

	public DefaultUserDetails(User user, List<LessonCell> lessonCellList) {
		this.user = user;
		grantedAuthorities = new HashSet<>();
		lessonCellList.stream()
			.map(LessonCellGrantedAuthority::new)
			.forEach(e -> grantedAuthorities.add(e));
		if (user.isAdmin()) {
			grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
		}
		grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return grantedAuthorities;
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	public User getUser() {
		return user;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}