package com.a6raywa1cher.rescheduletsuspring.models;

public enum WeekSign {
	ANY, PLUS, MINUS;

	public static WeekSign inverse(WeekSign weekSign) {
		if (weekSign == ANY) return ANY;
		return weekSign == PLUS ? MINUS : PLUS;
	}
}
