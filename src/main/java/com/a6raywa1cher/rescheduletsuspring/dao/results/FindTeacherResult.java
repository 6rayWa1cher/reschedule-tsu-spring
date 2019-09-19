package com.a6raywa1cher.rescheduletsuspring.dao.results;

import lombok.Data;

@Data
public class FindTeacherResult {
	private String teacherName;

	public FindTeacherResult(String teacherName) {
		this.teacherName = teacherName;
	}
}
