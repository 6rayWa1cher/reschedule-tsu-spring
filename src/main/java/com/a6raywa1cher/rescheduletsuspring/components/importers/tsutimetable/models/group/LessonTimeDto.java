package com.a6raywa1cher.rescheduletsuspring.components.importers.tsutimetable.models.group;

import lombok.Data;

import java.time.LocalTime;

@Data
public class LessonTimeDto {
	private LocalTime start;

	private LocalTime end;
}
