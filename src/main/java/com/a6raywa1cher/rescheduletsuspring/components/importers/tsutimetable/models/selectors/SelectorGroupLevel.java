package com.a6raywa1cher.rescheduletsuspring.components.importers.tsutimetable.models.selectors;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SelectorGroupLevel {
	BACHELOR("bachelor"),
	SPECIALTY("specialty"),
	MASTER("master"),
	POSTGRADUATE("postgraduate"),

	@JsonEnumDefaultValue
	UNKNOWN("");

	private final String id;

	SelectorGroupLevel(String id) {
		this.id = id;
	}

	@JsonValue
	public String getId() {
		return id;
	}
}
