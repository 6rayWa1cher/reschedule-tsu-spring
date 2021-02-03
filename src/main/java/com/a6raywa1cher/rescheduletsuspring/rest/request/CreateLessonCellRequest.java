package com.a6raywa1cher.rescheduletsuspring.rest.request;

import com.a6raywa1cher.rescheduletsuspring.config.WhiteSpaceRemovalDeserializer;
import com.a6raywa1cher.rescheduletsuspring.externalmodels.Direction;
import com.a6raywa1cher.rescheduletsuspring.models.LessonCellInfoHolder;
import com.a6raywa1cher.rescheduletsuspring.models.User;
import com.a6raywa1cher.rescheduletsuspring.models.WeekSign;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
public class CreateLessonCellRequest implements LessonCellInfoHolder {
	@NotNull
	private WeekSign weekSign;

	@NotBlank
	@Size(max = 255)
	@Pattern(regexp = "[a-zA-Zа-яА-Я _\\-',.()]{3,150}")
	@JsonDeserialize(using = WhiteSpaceRemovalDeserializer.class)
	private String fullSubjectName;

	@NotBlank
	@Size(max = 255)
	@Pattern(regexp = "[a-zA-Zа-яА-Я _\\-',.()]{3,150}")
	@JsonDeserialize(using = WhiteSpaceRemovalDeserializer.class)
	private String shortSubjectName;

	@Size(max = 255)
	@Pattern(regexp = "[a-zA-Zа-яА-Я _\\-',.]{0,100}")
	@JsonDeserialize(using = WhiteSpaceRemovalDeserializer.class)
	private String teacherName;

	@Size(max = 255)
	@Pattern(regexp = "[a-zA-Zа-яА-Я _\\-',.]{0,100}")
	@JsonDeserialize(using = WhiteSpaceRemovalDeserializer.class)
	private String teacherTitle;

	@NotNull
	private DayOfWeek dayOfWeek;

	@NotNull
	@Min(0)
	@Max(9)
	private Integer columnPosition;

	@NotNull
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private LocalTime start;

	@NotNull
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private LocalTime end;

	@Pattern(regexp = "[0-9А-Я]\\|[0-9а-яА-Я ,./\\-]{1,20}")
	@JsonDeserialize(using = WhiteSpaceRemovalDeserializer.class)
	private String auditoryAddress;

	@NotNull
	private Direction.Level level;

	@NotNull
	@Min(1)
	@Max(6)
	private Integer course;

	@NotBlank
	@Size(max = 150)
	@Pattern(regexp = "[а-яА-Я, \\-0-9':.(М)]{1,150}")
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
	@Size(max = 50)
	@Pattern(regexp = "[а-яА-Яa-zA-Z, \\-0-9()]{3,50}")
	private String faculty;

	@Size(max = 5)
	private List<String> attributes;

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
