package com.a6raywa1cher.rescheduletsuspring.service.submodels;

import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import com.a6raywa1cher.rescheduletsuspring.models.WeekSign;
import lombok.Data;

import java.time.DayOfWeek;
import java.util.List;

@Data
public class DaySchedule {
	private DayOfWeek dayOfWeek;
	private WeekSign sign;
	private List<LessonCell> cells;
}
