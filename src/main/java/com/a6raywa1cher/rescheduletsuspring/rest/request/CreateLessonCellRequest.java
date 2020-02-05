package com.a6raywa1cher.rescheduletsuspring.rest.request;

import com.a6raywa1cher.rescheduletsuspring.externalmodels.Direction;
import com.a6raywa1cher.rescheduletsuspring.models.LessonCellInfoHolder;
import com.a6raywa1cher.rescheduletsuspring.models.User;
import com.a6raywa1cher.rescheduletsuspring.models.WeekSign;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class CreateLessonCellRequest implements LessonCellInfoHolder {
	@NotNull
	private WeekSign weekSign;

	@NotBlank
	@Size(max = 255)
	private String fullSubjectName;

	@NotBlank
	@Size(max = 255)
	private String shortSubjectName;

	@Size(max = 255)
	private String teacherName;

	@Size(max = 255)
	private String teacherTitle;

	@NotNull
	private DayOfWeek dayOfWeek;

	@NotNull
	@PositiveOrZero
	private Integer columnPosition;

	@NotNull
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private LocalTime start;

	@NotNull
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private LocalTime end;

	@Pattern(regexp = ".*\\|.*")
	@Size(max = 15)
	private String auditoryAddress;

	@NotNull
	private Direction.Level level;

	@NotNull
	@Min(1)
	@Max(6)
	private Integer course;

	@NotBlank
	@Size(max = 255)
	private String group;

	@NotNull
	@Min(0)
	@Max(10)
	private Integer subgroup;

	@NotNull
	@Min(0)
	@Max(10)
	private Integer countOfSubgroups;

	@NotNull
	private Boolean crossPair;

	@NotBlank
	@Size(max = 255)
	private String faculty;

	@NotNull
	private Boolean ignoreExternalDb;

	@NotNull
	private Boolean ignoreLastExternalDbRecord;

	@JsonIgnore
	@Override
	public String getExternalId() {
		return UUID.randomUUID().toString();
	}

	@JsonIgnore
	@Override
	public void setExternalId(String externalId) {

	}

	@JsonIgnore
	@Override
	public User getCreator() {
		return null;
	}

	@JsonIgnore
	@Override
	public void setCreator(User creator) {

	}

	@JsonIgnore
	@Override
	public String getIgnoreExternalDbHashCode() {
		return null;
	}

	@JsonIgnore
	@Override
	public void setIgnoreExternalDbHashCode(String ignoreExternalDbHashCode) {
		ignoreLastExternalDbRecord = ignoreLastExternalDbRecord != null;
	}
}
