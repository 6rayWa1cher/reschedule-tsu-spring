package com.a6raywa1cher.rescheduletsuspring.externalmodels;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;

import java.util.stream.Stream;

@Data
public class Direction {
	private String _id;
	private int used; // ?
	private String code; // direction code in "nn.nn.nn"
	private String name; // full name
	private String abbr; // short name
	private String profile; // profile name
	private String faculty; // faculty id
	private Level level; // study level
	private int __v;

	@JsonSetter("level")
	public void setLevel(String deserializationName) {
		level = Level.getLevel(deserializationName);
	}

	public enum Level {
		BACHELOR_SPECIALTY("Бакалавриат | Специалитет"),
		MAGISTRACY("Магистратура");
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
}
