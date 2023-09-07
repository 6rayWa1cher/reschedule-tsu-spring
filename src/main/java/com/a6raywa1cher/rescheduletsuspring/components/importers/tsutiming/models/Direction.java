package com.a6raywa1cher.rescheduletsuspring.components.importers.tsutiming.models;

import com.a6raywa1cher.rescheduletsuspring.models.Level;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;

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
}
