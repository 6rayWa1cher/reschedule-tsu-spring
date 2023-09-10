package com.a6raywa1cher.rescheduletsuspring.service.interfaces;

import com.a6raywa1cher.rescheduletsuspring.models.Semester;

public interface CalendarService {

	String getCalendarForGroup(String faculty, String group, int studyYear, Semester semester);
}
