package com.a6raywa1cher.rescheduletsuspring.components.importers.tsutimetable.models.selectors;

import lombok.Data;

@Data
public class SelectorGroupDto {
	private Long groupId;

	private String groupName;

	private Boolean shortStudy;

	private Boolean individualPlan;

	private Long facultyId;

	private String facultyName;

	private Long specialityId;

	private String specialityName;

	private SelectorGroupLevel levelId;

	private String levelName;

	private Long studyYearId;

	private String studyYearName;

	private String formsOfTradingId;

	private String formsOfTradingName;
}
