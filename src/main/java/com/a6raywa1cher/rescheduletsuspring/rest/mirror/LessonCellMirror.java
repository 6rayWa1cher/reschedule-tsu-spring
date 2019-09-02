package com.a6raywa1cher.rescheduletsuspring.rest.mirror;

import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import com.a6raywa1cher.rescheduletsuspring.models.Week;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
public class LessonCellMirror {
//	private String externalId;

	private Week week;

	private String fullSubjectName;

	private String shortSubjectName;

	private String teacherName;

	private String teacherTitle;

	private DayOfWeek dayOfWeek;

	private Integer columnPosition;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "hh:mm")
	private LocalTime start;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "hh:mm")
	private LocalTime end;

	private String auditoryAddress;

	private String group;

	private Integer subgroup;

	private String faculty;

	@SuppressWarnings("DuplicatedCode")
	public static LessonCellMirror convert(LessonCell cell) {
		LessonCellMirror mirror = new LessonCellMirror();
		mirror.setWeek(cell.getWeek());
		mirror.setFullSubjectName(cell.getFullSubjectName());
		mirror.setShortSubjectName(cell.getShortSubjectName());
		mirror.setTeacherName(cell.getTeacherName());
		mirror.setTeacherTitle(cell.getTeacherTitle());
		mirror.setDayOfWeek(cell.getDayOfWeek());
		mirror.setColumnPosition(cell.getColumnPosition());
		mirror.setStart(cell.getStart());
		mirror.setEnd(cell.getEnd());
		mirror.setAuditoryAddress(cell.getAuditoryAddress());
		mirror.setGroup(cell.getGroup());
		mirror.setSubgroup(cell.getSubgroup());
		mirror.setFaculty(cell.getFaculty());
		return mirror;
	}
}
