package com.a6raywa1cher.rescheduletsuspring.components.importers.tsutimetable;

import com.a6raywa1cher.rescheduletsuspring.components.importers.tsutimetable.models.group.GroupScheduleDto;
import com.a6raywa1cher.rescheduletsuspring.components.importers.tsutimetable.models.group.LessonDto;
import com.a6raywa1cher.rescheduletsuspring.components.importers.tsutimetable.models.group.LessonTimeDto;
import com.a6raywa1cher.rescheduletsuspring.components.importers.tsutimetable.models.selectors.SelectorGroupDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LessonCellTimetableMapperContext {
	private final GroupScheduleDto groupScheduleDto;

	private final LessonDto lessonDto;

	private final LessonTimeDto lessonTimeDto;

	private final SelectorGroupDto selectorGroupDto;

	private final String professor;
}
