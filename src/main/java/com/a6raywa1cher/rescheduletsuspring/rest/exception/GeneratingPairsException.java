package com.a6raywa1cher.rescheduletsuspring.rest.exception;

import com.a6raywa1cher.rescheduletsuspring.models.WeekSign;

import java.time.DayOfWeek;
import java.util.Date;

public class GeneratingPairsException extends RuntimeException {
	public GeneratingPairsException(Date date, DayOfWeek dayOfWeek, WeekSign weekSign) {
		super(String.format("Creating pairs exception on date %s, day of week:%s, sign:%s",
			date.toString(), dayOfWeek, weekSign));
	}
}
