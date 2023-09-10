package com.a6raywa1cher.rescheduletsuspring.models;

import lombok.Getter;

@Getter
public enum Semester {
	FALL("Осень"), SPRING("Весна");

	private final String name;

	Semester(String name) {
		this.name = name;
	}

	public static Semester fromName(String name) {
		for (Semester semester : values()) {
			if (semester.getName().equalsIgnoreCase(name)) {
				return semester;
			}
		}
		throw new IllegalArgumentException("No such semester: " + name);
	}
}
