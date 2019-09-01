package com.a6raywa1cher.rescheduletsuspring.externalmodels;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class Timetable {
	private String _id;

	private String year; // current study year in format "2018-2019"

	private String semester; // half year flag "Осень" and "Весна" as autumn and spring respectively

	private int course; // year of study, for undergraduate magisters counter resets

	private String groupName; // "(М)NN __short_name__ (__subinfo__)", see direction

	private OffsetDateTime start; // time in ISO-8601 like 2019-01-01T20:20:39.000Z

	private OffsetDateTime end; // time in ISO-8601 like 2019-01-01T20:20:39.000Z

	@IdExternal(url = "times", toSetter = "setTimeSchedule", clazz = TimeSchedule.class)
	private String time; // id->timeSchedule

	private List<TimetableCell> cells;

	private List<String> subgroups; // unused

	private Faculty faculty;

	private Direction direction;

	private int __v;


	@JsonIgnore
	private TimeSchedule timeSchedule;
}
