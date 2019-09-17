package com.a6raywa1cher.rescheduletsuspring.components.tsudbimporter;

import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import com.a6raywa1cher.rescheduletsuspring.models.WeekSign;
import lombok.Data;

import java.time.DayOfWeek;

@Data
public class CrossPairLessonCell {
	private String facultyId;

	private WeekSign weekSign;

	private DayOfWeek dayOfWeek;

	private Integer columnPosition;

	private String teacherName;

	public static CrossPairLessonCell convert(LessonCell cell) {
		CrossPairLessonCell crossPair = new CrossPairLessonCell();
		crossPair.setFacultyId(cell.getFaculty());
		crossPair.setWeekSign(cell.getWeekSign());
		crossPair.setDayOfWeek(cell.getDayOfWeek());
		crossPair.setColumnPosition(cell.getColumnPosition());
		crossPair.setTeacherName(cell.getTeacherName());
		return crossPair;
	}
}
