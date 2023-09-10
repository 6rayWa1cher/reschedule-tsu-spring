package com.a6raywa1cher.rescheduletsuspring.service.impl;

import com.a6raywa1cher.rescheduletsuspring.components.weeksign.WeekSignComponent;
import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import com.a6raywa1cher.rescheduletsuspring.models.Semester;
import com.a6raywa1cher.rescheduletsuspring.models.WeekSign;
import com.a6raywa1cher.rescheduletsuspring.service.interfaces.CalendarService;
import com.a6raywa1cher.rescheduletsuspring.service.interfaces.LessonCellService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.property.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static net.fortuna.ical4j.model.property.Status.VALUE_CONFIRMED;

@Service
@AllArgsConstructor
@Slf4j
public class CalendarServiceImpl implements CalendarService {
	private static final int EVERY_WEEK_INTERVAL = 1;

	private static final int ONCE_PER_TWO_WEEKS_INTERVAL = 2;

	private final LessonCellService lessonCellService;

	private final WeekSignComponent weekSignComponent;

	@Override
	@SneakyThrows
	public String getCalendarForGroup(String faculty, String group, int studyYear, Semester semester) {
		List<LessonCell> lessonCells = lessonCellService.getAllByGroup(group, faculty);

		Calendar calendar = new Calendar()
			.withProperty(new ProdId("-//reschedule-tsu-spring//iCal4j 1.0//RU").getFluentTarget())
			.withProperty(Version.VERSION_2_0)
			.withProperty(CalScale.GREGORIAN)
			.getFluentTarget();

		for (LessonCell lessonCell : lessonCells) {
			try {
				Instant firstLessonOfSubjectStartInstant = getStartDate(
					faculty, studyYear, semester, lessonCell.getWeekSign(),
					lessonCell.getDayOfWeek(), lessonCell.getStart()
				);
				Instant firstLessonOfSubjectEndInstant = firstLessonOfSubjectStartInstant.plus(
					Duration.between(lessonCell.getStart(), lessonCell.getEnd())
				);

				VEvent event = new VEvent(
					new DateTime(java.util.Date.from(firstLessonOfSubjectStartInstant), null),
					new DateTime(java.util.Date.from(firstLessonOfSubjectEndInstant), null),
					lessonCell.getFullSubjectName()
				)
					.withProperty(new Uid(lessonCell.getExternalId()).getFluentTarget())
					.withProperty(new RRule(String.format(
						"FREQ=WEEKLY;INTERVAL=%d;UNTIL=%s",
						mapWeekSignToInterval(lessonCell.getWeekSign()),
						getLastDateOfSemesterInUntilFormat(studyYear, semester)
					)).getFluentTarget())
					.withProperty(new Attendee()
						.withParameter(Role.REQ_PARTICIPANT)
						.withParameter(new Cn(lessonCell.getTeacherName()))
						.getFluentTarget())
					.withProperty(new Location(getLocationString(lessonCell.getAuditoryAddress()))
						.getFluentTarget())
					.withProperty(new Status(VALUE_CONFIRMED))
					.getFluentTarget();

				calendar.getComponents().add(event);
			} catch (Exception e) {
				log.error("Calendar event generation error on " + lessonCell.toString(), e);
			}
		}

		return calendar.toString();
	}

	private int mapWeekSignToInterval(WeekSign weekSign) {
		return weekSign == WeekSign.ANY ? EVERY_WEEK_INTERVAL : ONCE_PER_TWO_WEEKS_INTERVAL;
	}

	private Instant getStartDate(
		String faculty, int studyYear, Semester semester, WeekSign weekSign, DayOfWeek dayOfWeek, LocalTime time
	) {
		LocalDate startDate = semester == Semester.FALL ?
			LocalDate.of(studyYear, Month.SEPTEMBER, 1) :
			LocalDate.of(studyYear + 1, Month.FEBRUARY, 1);
		while (startDate.getDayOfWeek() != dayOfWeek ||
			(weekSign != WeekSign.ANY && weekSignComponent.getWeekSign(Date.from(
				startDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()
			), faculty) != weekSign)) {
			startDate = startDate.plusDays(1);
		}
		LocalDateTime localDateTime = LocalDateTime.of(startDate, time);
		return localDateTime.toInstant(ZoneId.systemDefault().getRules().getOffset(localDateTime));
	}

	private String getLastDateOfSemesterInUntilFormat(int studyYear, Semester semester) {
		LocalDate localDate = semester == Semester.FALL ?
			LocalDate.of(studyYear + 1, Month.JANUARY, 1) :
			LocalDate.of(studyYear + 1, Month.JUNE, 1);

		return localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "T000000Z";
	}

	private String getLocationString(String location) {
		String[] split = location.split("\\|");
		if (split.length != 2) return location;
		if ("-".equals(split[0])) return location;
		return String.format("ауд.%s, к.%s", split[1], split[0]);
	}
}
