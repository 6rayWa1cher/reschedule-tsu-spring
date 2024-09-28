package com.a6raywa1cher.rescheduletsuspring.components.importers.tsutimetable.mappers;

import com.a6raywa1cher.rescheduletsuspring.components.importers.ImportException;
import com.a6raywa1cher.rescheduletsuspring.components.importers.tsutimetable.LessonCellTimetableMapper;
import com.a6raywa1cher.rescheduletsuspring.components.importers.tsutimetable.LessonCellTimetableMapperContext;
import com.a6raywa1cher.rescheduletsuspring.components.importers.tsutimetable.models.group.LessonDto;
import com.a6raywa1cher.rescheduletsuspring.components.importers.tsutimetable.models.selectors.SelectorGroupDto;
import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import com.a6raywa1cher.rescheduletsuspring.models.Level;
import org.springframework.stereotype.Component;

@Component
public class GroupDataLessonCellTimetableMapper implements LessonCellTimetableMapper {
	@Override
	public void map(LessonCellTimetableMapperContext ctx, LessonCell cell) throws ImportException {
		LessonDto lessonDto = ctx.getLessonDto();
		SelectorGroupDto selectorGroupDto = ctx.getSelectorGroupDto();

		cell.setFaculty(selectorGroupDto.getFacultyName());
		cell.setGroup(selectorGroupDto.getGroupName());
		cell.setSubgroup(mapSubgroup(lessonDto.getSize(), lessonDto.getPosition()));
		cell.setCourse(1); // TODO: remove field
		cell.setLevel(Level.BACHELOR_SPECIALTY); // TODO: remove field
		cell.setCountOfSubgroups(2);
	}

	private int mapSubgroup(Integer size, Integer position) {
		if (size == null || position == null) return 0;

		if (size > 2) return 0;

		return size == 1 ? 0 : position;
	}
}
