package com.a6raywa1cher.rescheduletsuspring.models;

import java.util.stream.Stream;

public enum Level {
	BACHELOR_SPECIALTY("Бакалавриат | Специалитет"),
	MAGISTRACY("Магистратура"),
	POSTGRADUATE("Аспирантура");

	private final String deserializationName;

	Level(String deserializationName) {
		this.deserializationName = deserializationName;
	}

	public static Level getLevel(String deserializationName) {
		if (deserializationName == null) return null;
		return Stream.of(Level.values())
			.filter(lvl -> lvl.getDeserializationName().equals(deserializationName.strip()))
			.findAny().orElseThrow();
	}

	public String getDeserializationName() {
		return deserializationName;
	}
}
