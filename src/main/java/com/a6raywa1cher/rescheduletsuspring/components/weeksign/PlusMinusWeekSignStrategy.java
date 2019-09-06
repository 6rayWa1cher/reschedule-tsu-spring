package com.a6raywa1cher.rescheduletsuspring.components.weeksign;

import com.a6raywa1cher.rescheduletsuspring.models.WeekSign;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class PlusMinusWeekSignStrategy implements WeekSignStrategy {
	@Override
	public WeekSign getWeekSign(Date date, LocalDate startDate, WeekSign startWeekSign) {
		Calendar current = Calendar.getInstance();
		current.setTime(date);
		Calendar start = Calendar.getInstance();
		start.setTime(Date.from(startDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
		int startWeek = start.get(Calendar.WEEK_OF_YEAR);
		int currWeek = current.get(Calendar.WEEK_OF_YEAR);
		int delta = currWeek - startWeek;
		return delta % 2 == 0 ? startWeekSign : (startWeekSign == WeekSign.PLUS ? WeekSign.MINUS : WeekSign.PLUS);
	}
}
