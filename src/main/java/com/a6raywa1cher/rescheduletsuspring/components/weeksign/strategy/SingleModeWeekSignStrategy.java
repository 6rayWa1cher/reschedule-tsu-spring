package com.a6raywa1cher.rescheduletsuspring.components.weeksign.strategy;

import com.a6raywa1cher.rescheduletsuspring.components.weeksign.WeekSignStrategy;
import com.a6raywa1cher.rescheduletsuspring.models.WeekSign;

import java.time.LocalDate;
import java.util.Date;

public class SingleModeWeekSignStrategy implements WeekSignStrategy {
	@Override
	public WeekSign getWeekSign(Date date, LocalDate startDate, WeekSign startWeekSign) {
		return WeekSign.ANY;
	}
}
