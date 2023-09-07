package com.a6raywa1cher.rescheduletsuspring.components.importers.tsutiming.models;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Stream;

@Data
public class TimetableCell {
	private String _id;
	private DayOfWeek day; //day of week
	private int number; // position of cell, in coordination with TimeSchedule, starting from 0
	private OffsetDateTime edited; // last edit time
	private List<Lesson> lessons;

	@JsonSetter("day")
	public void setDay(String deserializationName) {
		day = DayOfWeek.getDayOfWeek(deserializationName);
	}

	public enum DayOfWeek {
		SUNDAY("Воскресенье", java.time.DayOfWeek.SUNDAY),
		MONDAY("Понедельник", java.time.DayOfWeek.MONDAY),
		TUESDAY("Вторник", java.time.DayOfWeek.TUESDAY),
		WEDNESDAY("Среда", java.time.DayOfWeek.WEDNESDAY),
		THURSDAY("Четверг", java.time.DayOfWeek.THURSDAY),
		FRIDAY("Пятница", java.time.DayOfWeek.FRIDAY),
		SATURDAY("Суббота", java.time.DayOfWeek.SATURDAY);
		private final String deserializationName;
		private java.time.DayOfWeek javaDayOfWeek;

		DayOfWeek(String deserializationName, java.time.DayOfWeek javaDayOfWeek) {
			this.deserializationName = deserializationName;
			this.javaDayOfWeek = javaDayOfWeek;
		}

		public static TimetableCell.DayOfWeek getDayOfWeek(String deserializationName) {
			return Stream.of(TimetableCell.DayOfWeek.values())
					.filter(lvl -> lvl.getDeserializationName().equals(deserializationName.strip()))
					.findAny().orElse(null);
		}

		public String getDeserializationName() {
			return deserializationName;
		}

		public java.time.DayOfWeek getJavaDayOfWeek() {
			return javaDayOfWeek;
		}
	}
}
