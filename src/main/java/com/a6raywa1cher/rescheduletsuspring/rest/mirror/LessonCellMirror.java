package com.a6raywa1cher.rescheduletsuspring.rest.mirror;

import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import com.a6raywa1cher.rescheduletsuspring.models.WeekSign;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
public class LessonCellMirror {
	@JsonView(View.Internal.class)
	private String externalId;

	@JsonView(View.Public.class)
	private WeekSign weekSign;

	@JsonView(View.Public.class)
	private String fullSubjectName;

	@JsonView(View.Public.class)
	private String shortSubjectName;

	@JsonView(View.Public.class)
	private String teacherName;

	@JsonView(View.Public.class)
	private String teacherTitle;

	@JsonView(View.Public.class)
	private DayOfWeek dayOfWeek;

	@JsonView(View.Public.class)
	private Integer columnPosition;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
	@JsonView(View.Public.class)
	private LocalTime start;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
	@JsonView(View.Public.class)
	private LocalTime end;

	@JsonView(View.Public.class)
	private String auditoryAddress;

	@JsonView(View.Public.class)
	private Integer course;

	@JsonView(View.Public.class)
	private String group;

	@JsonView(View.Public.class)
	private Integer subgroup;

	@JsonView(View.Public.class)
	private Boolean crossPair;

	@JsonView(View.Public.class)
	private String faculty;

	@JsonView(View.Public.class)
	private Boolean userMade;

	@JsonView(View.Internal.class)
	private Boolean ignoreExternalDb;

	@JsonView(View.Internal.class)
	private Boolean ignoreExternalDbByHashCode;

	@SuppressWarnings("DuplicatedCode")
	public static LessonCellMirror convert(LessonCell cell) {
		LessonCellMirror mirror = new LessonCellMirror();
		mirror.setExternalId(cell.getExternalId());
		mirror.setWeekSign(cell.getWeekSign());
		mirror.setFullSubjectName(cell.getFullSubjectName());
		mirror.setShortSubjectName(cell.getShortSubjectName());
		mirror.setTeacherName(cell.getTeacherName());
		mirror.setTeacherTitle(cell.getTeacherTitle());
		mirror.setDayOfWeek(cell.getDayOfWeek());
		mirror.setColumnPosition(cell.getColumnPosition());
		mirror.setStart(cell.getStart());
		mirror.setEnd(cell.getEnd());
		mirror.setAuditoryAddress(cell.getAuditoryAddress());
		mirror.setCourse(cell.getCourse());
		mirror.setGroup(cell.getGroup());
		mirror.setSubgroup(cell.getSubgroup());
		mirror.setCrossPair(cell.getCrossPair());
		mirror.setFaculty(cell.getFaculty());
		mirror.setUserMade(cell.getCreator() != null);
		mirror.setIgnoreExternalDb(cell.getIgnoreExternalDb());
		mirror.setIgnoreExternalDbByHashCode(cell.getIgnoreExternalDbHashCode() != null);
		return mirror;
	}
}
