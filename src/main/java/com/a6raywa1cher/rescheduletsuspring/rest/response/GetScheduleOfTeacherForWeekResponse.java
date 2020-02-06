package com.a6raywa1cher.rescheduletsuspring.rest.response;

import com.a6raywa1cher.rescheduletsuspring.models.WeekSign;
import com.a6raywa1cher.rescheduletsuspring.rest.mirror.LessonCellMirror;
import com.a6raywa1cher.rescheduletsuspring.rest.mirror.View;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

@Data
public class GetScheduleOfTeacherForWeekResponse {
	@JsonView(View.Public.class)
	private List<GetScheduleOfTeacherForWeekResponse.Schedule> schedules = new ArrayList<>();

	@Data
	public static final class Schedule {
		@JsonView(View.Public.class)
		private DayOfWeek dayOfWeek;
		@JsonView(View.Public.class)
		private WeekSign sign;
		@JsonView(View.Public.class)
		private List<LessonCellMirror> cells;
	}
}
