package com.a6raywa1cher.rescheduletsuspring.components.importers.tsutimetable;

import com.a6raywa1cher.rescheduletsuspring.components.importers.ImportException;
import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;

public interface LessonCellTimetableMapper {
	void map(LessonCellTimetableMapperContext ctx, LessonCell cell) throws ImportException;
}
