package com.a6raywa1cher.rescheduletsuspring.components.importers.tsutimetable.mappers;

import com.a6raywa1cher.rescheduletsuspring.components.importers.ImportException;
import com.a6raywa1cher.rescheduletsuspring.components.importers.tsutimetable.LessonCellTimetableMapper;
import com.a6raywa1cher.rescheduletsuspring.components.importers.tsutimetable.LessonCellTimetableMapperContext;
import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import com.a6raywa1cher.rescheduletsuspring.models.WeekSign;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;

@Component
public class TimeDataLessonCellTimetableMapper implements LessonCellTimetableMapper {
	private static final int LESSON_NUMBER_SHIFT = 1;

	@Override
	public void map(LessonCellTimetableMapperContext ctx, LessonCell cell) throws ImportException {
		cell.setWeekSign(mapWeekMarkToWeekSign(ctx.getLessonDto().getWeekMark()));
		cell.setDayOfWeek(DayOfWeek.of(ctx.getLessonDto().getWeekDay()));
		cell.setColumnPosition(ctx.getLessonDto().getLessonNumber() + LESSON_NUMBER_SHIFT);
		cell.setStart(ctx.getLessonTimeDto().getStart());
		cell.setEnd(ctx.getLessonTimeDto().getEnd());
	}

	private WeekSign mapWeekMarkToWeekSign(String weekMark) throws ImportException {
		switch (weekMark) {
			case "every":
				return WeekSign.ANY;
			case "plus":
				return WeekSign.PLUS;
			case "minus":
				return WeekSign.MINUS;
			default:
				throw new ImportException("Unknown week mark: " + weekMark);
		}
	}
}
