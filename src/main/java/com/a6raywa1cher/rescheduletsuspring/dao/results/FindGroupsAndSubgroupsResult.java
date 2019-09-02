package com.a6raywa1cher.rescheduletsuspring.dao.results;

import lombok.Data;

@Data
public class FindGroupsAndSubgroupsResult {
	private String group;

	private Integer subgroups;

	public FindGroupsAndSubgroupsResult(String group, Integer subgroups) {
		this.group = group;
		this.subgroups = subgroups;
	}
}
