package com.a6raywa1cher.rescheduletsuspring.rest.response;

import com.a6raywa1cher.rescheduletsuspring.models.LessonCell;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class GetGroupsResponse {
	private List<GroupInfo> groups;

	@Data
	@AllArgsConstructor
	public static final class GroupInfo {
		private String name;
		private Integer subgroups;
		private Integer course;
		private List<LessonCell> lessonCells;
	}
}
