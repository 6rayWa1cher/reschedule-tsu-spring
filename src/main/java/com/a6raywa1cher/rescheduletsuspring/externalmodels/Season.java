package com.a6raywa1cher.rescheduletsuspring.externalmodels;

import lombok.Data;

import java.util.List;

@Data
public class Season {
	private Info _id;
	private List<Timetable> tables;

	@Data
	public static class Info {
		private String year;
		private String semester;
		private Faculty faculty;
	}
}
