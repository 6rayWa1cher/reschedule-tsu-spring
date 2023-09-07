package com.a6raywa1cher.rescheduletsuspring.models.submodels;

import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import com.a6raywa1cher.rescheduletsuspring.models.WeekSign;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonCellCoordinates {
	private String facultyId;

	private String group;

	private Integer subgroup;

	private WeekSign weekSign;

	private DayOfWeek dayOfWeek;

	private Integer columnPosition;

	private String teacherName;

	public static LessonCellCoordinates convert(LessonCell cell) {
		LessonCellCoordinates coordinates = new LessonCellCoordinates();
		coordinates.setFacultyId(cell.getFaculty());
		coordinates.setGroup(cell.getGroup());
		coordinates.setSubgroup(cell.getSubgroup());
		coordinates.setWeekSign(cell.getWeekSign());
		coordinates.setDayOfWeek(cell.getDayOfWeek());
		coordinates.setColumnPosition(cell.getColumnPosition());
		coordinates.setTeacherName(cell.getTeacherName());
		return coordinates;
	}

	public String toIdentifier() {
		return String.format(
			"%s_%s_%d_%s_%s_%d_%s",
			facultyId, group, subgroup, weekSign.name(), dayOfWeek.name(), columnPosition, teacherName
		);
	}
}
