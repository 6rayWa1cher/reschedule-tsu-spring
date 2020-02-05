package com.a6raywa1cher.rescheduletsuspring.models;

import com.a6raywa1cher.rescheduletsuspring.externalmodels.Direction;

import java.time.DayOfWeek;
import java.time.LocalTime;

public interface LessonCellInfoHolder {
	String getExternalId();

	void setExternalId(String externalId);

	WeekSign getWeekSign();

	void setWeekSign(WeekSign weekSign);

	String getFullSubjectName();

	void setFullSubjectName(String fullSubjectName);

	String getShortSubjectName();

	void setShortSubjectName(String shortSubjectName);

	String getTeacherName();

	void setTeacherName(String teacherName);

	String getTeacherTitle();

	void setTeacherTitle(String teacherTitle);

	DayOfWeek getDayOfWeek();

	void setDayOfWeek(DayOfWeek dayOfWeek);

	Integer getColumnPosition();

	void setColumnPosition(Integer columnPosition);

	LocalTime getStart();

	void setStart(LocalTime start);

	LocalTime getEnd();

	void setEnd(LocalTime end);

	String getAuditoryAddress();

	void setAuditoryAddress(String auditoryAddress);

	Direction.Level getLevel();

	void setLevel(Direction.Level level);

	Integer getCourse();

	void setCourse(Integer course);

	String getGroup();

	void setGroup(String group);

	Integer getSubgroup();

	void setSubgroup(Integer subgroup);

	Integer getCountOfSubgroups();

	void setCountOfSubgroups(Integer countOfSubgroups);

	Boolean getCrossPair();

	void setCrossPair(Boolean crossPair);

	String getFaculty();

	void setFaculty(String faculty);

	User getCreator();

	void setCreator(User creator);

	Boolean getIgnoreExternalDb();

	void setIgnoreExternalDb(Boolean ignoreExternalDb);

	String getIgnoreExternalDbHashCode();

	void setIgnoreExternalDbHashCode(String ignoreExternalDbHashCode);

	default void transfer(LessonCellInfoHolder other) {
		this.setExternalId(other.getExternalId());
		this.setWeekSign(other.getWeekSign());
		this.setFullSubjectName(other.getFullSubjectName());
		this.setShortSubjectName(other.getShortSubjectName());
		this.setTeacherName(other.getTeacherName());
		this.setTeacherTitle(other.getTeacherTitle());
		this.setDayOfWeek(other.getDayOfWeek());
		this.setColumnPosition(other.getColumnPosition());
		this.setStart(other.getStart());
		this.setEnd(other.getEnd());
		this.setAuditoryAddress(other.getAuditoryAddress());
		this.setLevel(other.getLevel());
		this.setCourse(other.getCourse());
		this.setGroup(other.getGroup());
		this.setSubgroup(other.getSubgroup());
		this.setCountOfSubgroups(other.getCountOfSubgroups());
		this.setCrossPair(other.getCrossPair());
		this.setFaculty(other.getFaculty());
		this.setCreator(other.getCreator());
		this.setIgnoreExternalDb(other.getIgnoreExternalDb());
		this.setIgnoreExternalDbHashCode(other.getIgnoreExternalDbHashCode());
	}

	default LessonCellInfoHolder convert(LessonCellInfoHolder other) {
		throw new RuntimeException("Convert operation not allowed");
	}
}
