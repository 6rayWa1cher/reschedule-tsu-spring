package com.a6raywa1cher.rescheduletsuspring.rest.response;

import com.a6raywa1cher.rescheduletsuspring.models.WeekSign;
import com.a6raywa1cher.rescheduletsuspring.rest.mirror.LessonCellMirror;
import lombok.Data;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

@Data
public class GetScheduleForWeekResponse {
	private List<Schedule> schedules = new ArrayList<>();

	@Data
	public static final class Schedule {
		private DayOfWeek dayOfWeek;
		private WeekSign sign;
		private List<LessonCellMirror> cells;
	}
}
