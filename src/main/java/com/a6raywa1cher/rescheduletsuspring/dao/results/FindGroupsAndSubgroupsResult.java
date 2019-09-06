package com.a6raywa1cher.rescheduletsuspring.dao.results;

import lombok.Data;

@Data
public class FindGroupsAndSubgroupsResult {
	private String group;

	private Integer subgroups;

	private Integer course;

	public FindGroupsAndSubgroupsResult(String group, Integer subgroups, Integer course) {
		this.group = group;
		this.subgroups = subgroups;
		this.course = course;
	}
}
