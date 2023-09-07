package com.a6raywa1cher.rescheduletsuspring.components.importers.tsutimetable.models.group;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class GroupScheduleDto {
	private String groupName;

	private Long lessonTimeId;

	private List<LessonTimeDto> lessonTimeData;

	private String types;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
	private LocalDate start;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
	private LocalDate finish;

	private Boolean withDates;

	private List<LessonDto> lessons;

	private Integer status;

	private String data;
}
