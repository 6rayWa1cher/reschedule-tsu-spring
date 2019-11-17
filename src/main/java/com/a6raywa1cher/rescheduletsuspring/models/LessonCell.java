package com.a6raywa1cher.rescheduletsuspring.models;

import com.a6raywa1cher.rescheduletsuspring.externalmodels.Direction;
import lombok.Data;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Entity
@Table(name = "lesson_cell", indexes = {
	@Index(columnList = "faculty,group", name = "Get_for_group")
})
public class LessonCell {
	@Id
	private String externalId;

	@Column
	private Integer semester;

	@Column
	@Enumerated(EnumType.ORDINAL)
	private WeekSign weekSign;

	@Column
	private String fullSubjectName;

	@Column
	private String shortSubjectName;

	@Column
	private String teacherName;

	@Column
	private String teacherTitle;

	@Column
	@Enumerated(EnumType.ORDINAL)
	private DayOfWeek dayOfWeek;

	@Column
	private Integer columnPosition;

	@Column
	private LocalTime start;

	@Column(name = "\"end\"")
	private LocalTime end;

	@Column
	private String auditoryAddress;

	@Column
	@Enumerated(EnumType.ORDINAL)
	private Direction.Level level;

	@Column
	private Integer course;

	@Column(name = "\"group\"")
	private String group;

	@Column
	private Integer subgroup;

	@Column
	private Integer countOfSubgroups;

	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean crossPair;

	@Column(nullable = false)
	private String faculty;


	// 2019 first semester -> 4038, second -> 4039
	public static int semesterToNumber(String years, String semester) {
		Matcher matcher = Pattern.compile("(\\d{4}) - \\d{4}").matcher(years);
		matcher.find();
		int firstYear = Integer.parseInt(matcher.group(1));
		boolean isFirstSemester = semester.equals("Осень");
		return firstYear * 2 + (isFirstSemester ? 0 : 1);
	}

	public static String numberToSemester(int number) {
		boolean isFirstSemester = number % 2 == 0;
		int firstYear = number / 2;
		int secondYear = firstYear + 1;
		return String.format("%d - %d %s", firstYear, secondYear, isFirstSemester ? "Осень" : "Весна");
	}
}
