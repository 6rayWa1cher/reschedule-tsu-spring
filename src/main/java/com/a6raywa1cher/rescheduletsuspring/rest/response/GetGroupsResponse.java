package com.a6raywa1cher.rescheduletsuspring.rest.response;

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
	}
}
