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
			int firstLeftParenAt = professor.indexOf('(');
			String teacherName = professor.substring(0, firstLeftParenAt).strip();
			String teacherTitle = professor.substring(firstLeftParenAt + 1).strip();
			cell.setTeacherName(teacherName);
			cell.setTeacherTitle(teacherTitle.substring(0, teacherTitle.length() - 1).strip());
		} else {
			cell.setTeacherName(professor.strip());
		}
	}
}
