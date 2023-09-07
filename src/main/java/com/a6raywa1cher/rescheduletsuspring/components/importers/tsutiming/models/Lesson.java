package com.a6raywa1cher.rescheduletsuspring.components.importers.tsutiming.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class Lesson {
	private String _id;
	private int subgroup; // 0 means all group.
	private String plus_minus; // sign of week, when this lesson passes.
	private String comment;
	@IdExternal(url = "auditories", toSetter = "setAuditoryObj", clazz = Auditory.class)
	private String auditory; // id -> auditoryObj
	@IdExternal(url = "teachers", toSetter = "setTeacherObj", clazz = Teacher.class)
	private String teacher; // id -> teacherObj
	@IdExternal(url = "subjects", toSetter = "setSubjectObj", clazz = Subject.class)
	private String subject; // id -> subjectObj

	@JsonIgnore
	private Teacher teacherObj;

	@JsonIgnore
	private Auditory auditoryObj;

	@JsonIgnore
	private Subject subjectObj;
}
