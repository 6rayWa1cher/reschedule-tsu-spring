package com.a6raywa1cher.rescheduletsuspring.components.importers.tsutimetable.mappers;

import com.a6raywa1cher.rescheduletsuspring.components.importers.ImportException;
import com.a6raywa1cher.rescheduletsuspring.components.importers.tsutimetable.LessonCellTimetableMapper;
import com.a6raywa1cher.rescheduletsuspring.components.importers.tsutimetable.LessonCellTimetableMapperContext;
import com.a6raywa1cher.rescheduletsuspring.components.importers.tsutimetable.models.group.LessonDto;
import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import org.springframework.stereotype.Component;

@Component
public class LessonDataLessonCellTimetableMapper implements LessonCellTimetableMapper {
	@Override
	public void map(LessonCellTimetableMapperContext ctx, LessonCell cell) throws ImportException {
		LessonDto lessonDto = ctx.getLessonDto();
		String professor = ctx.getProfessor();

		cell.setFullSubjectName(lessonDto.getCourseName().strip());
		cell.setShortSubjectName(lessonDto.getCourseName().strip());

		if (professor.matches(" *[^(]+\\(.+\\) *")) {
			String[] split = professor.strip().split("\\(");
			cell.setTeacherName(split[0].strip());
			cell.setTeacherTitle(split[1].strip().substring(0, split[1].length() - 1).strip());
		} else {
			cell.setTeacherName(professor.strip());
		}
	}
}
