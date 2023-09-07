package com.a6raywa1cher.rescheduletsuspring.components.importers.enhancer.units;

import com.a6raywa1cher.rescheduletsuspring.components.importers.enhancer.LessonCellEnhancerUnit;
import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import com.a6raywa1cher.rescheduletsuspring.models.WeekSign;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CrossPairLessonCellEnhancerUnit implements LessonCellEnhancerUnit {
	@Override
	public List<LessonCell> enhance(List<LessonCell> lessonCells) {
		Map<CrossPairLessonCellCoordinates, LessonCell> firstOccurrences = new HashMap<>();
		for (LessonCell lessonCell : lessonCells) {
			CrossPairLessonCellCoordinates crossPair = CrossPairLessonCellCoordinates.convert(lessonCell);
			if (firstOccurrences.containsKey(crossPair)) {
				firstOccurrences.get(crossPair).setCrossPair(true);
				lessonCell.setCrossPair(true);
			} else {
				firstOccurrences.put(crossPair, lessonCell);
				lessonCell.setCrossPair(false);
			}
		}
		return lessonCells;
	}


	@Data
	private static class CrossPairLessonCellCoordinates {
		private String facultyId;

		private WeekSign weekSign;

		private DayOfWeek dayOfWeek;

		private Integer columnPosition;

		private String teacherName;

		private static CrossPairLessonCellCoordinates convert(LessonCell cell) {
			CrossPairLessonCellCoordinates crossPair = new CrossPairLessonCellCoordinates();
			crossPair.setFacultyId(cell.getFaculty());
			crossPair.setWeekSign(cell.getWeekSign());
			crossPair.setDayOfWeek(cell.getDayOfWeek());
			crossPair.setColumnPosition(cell.getColumnPosition());
			crossPair.setTeacherName(cell.getTeacherName());
			return crossPair;
		}
	}
}
