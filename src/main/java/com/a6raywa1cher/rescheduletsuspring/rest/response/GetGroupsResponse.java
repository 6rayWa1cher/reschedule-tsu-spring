package com.a6raywa1cher.rescheduletsuspring.rest.response;

import com.a6raywa1cher.rescheduletsuspring.rest.mirror.LessonCellMirror;
import com.a6raywa1cher.rescheduletsuspring.rest.mirror.View;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class GetGroupsResponse {
	@JsonView(View.Public.class)
	private List<GroupInfo> groups;

	@Data
	@AllArgsConstructor
	public static final class GroupInfo {
		@JsonView(View.Public.class)
		private String level;
		@JsonView(View.Public.class)
		private String name;
		@JsonView(View.Public.class)
		private Integer subgroups;
		@JsonView(View.Public.class)
		private Integer course;
		@JsonView(View.Public.class)
		private List<LessonCellMirror> lessonCells;
	}
}
