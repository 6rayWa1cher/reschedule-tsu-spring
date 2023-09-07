package com.a6raywa1cher.rescheduletsuspring.components.importers.tsutimetable.models.group;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class LessonDto {
	private String lessonType;

	private String lessonTypeName;

	private Integer lessonNumber;

	private Integer weekDay;

	private String weekMark;

	private String weekMarkName;

	private Integer size;

	private Integer position;

	private String roomName;

	private String courseName;

	private Long rows;

	private String text;

	private String displayType;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
	private LocalDate date;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
	private LocalDate start;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
	private LocalDate finish;

	private List<String> professors;
}
