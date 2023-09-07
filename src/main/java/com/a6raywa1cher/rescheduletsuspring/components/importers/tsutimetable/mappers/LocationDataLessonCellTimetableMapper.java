package com.a6raywa1cher.rescheduletsuspring.components.importers.tsutimetable.mappers;

import com.a6raywa1cher.rescheduletsuspring.components.importers.ImportException;
import com.a6raywa1cher.rescheduletsuspring.components.importers.tsutimetable.LessonCellTimetableMapper;
import com.a6raywa1cher.rescheduletsuspring.components.importers.tsutimetable.LessonCellTimetableMapperContext;
import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class LocationDataLessonCellTimetableMapper implements LessonCellTimetableMapper {
	private static final Pattern ROOM_NAME_COMMON_PATTERN =
		Pattern.compile("^ *Учебный корпус[№\" ]*(?<b>[0-9а-яА-Яa-zA-Z])\"? *(-|ауд.)? *+(?<a>.+) *+$");

	@Override
	public void map(LessonCellTimetableMapperContext ctx, LessonCell cell) throws ImportException {
		cell.setAuditoryAddress(mapAuditoryAddress(ctx.getLessonDto().getRoomName()));
	}

	private String mapAuditoryAddress(String roomName) {
		if (roomName == null) return null;
		Matcher matcher = ROOM_NAME_COMMON_PATTERN.matcher(roomName);
		if (matcher.matches()) {
			String audiences = matcher.group("a");
			String building = matcher.group("b");
			return building.strip() + "|" + audiences.strip();
		}
		return "-|" + roomName.strip();
	}
}
