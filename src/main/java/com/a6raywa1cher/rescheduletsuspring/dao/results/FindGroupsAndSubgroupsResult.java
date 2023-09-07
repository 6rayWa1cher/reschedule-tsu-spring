package com.a6raywa1cher.rescheduletsuspring.dao.results;

import com.a6raywa1cher.rescheduletsuspring.models.Level;
import lombok.Data;

@Data
public class FindGroupsAndSubgroupsResult {
	private Level level;

	private String group;

	private Integer subgroups;

	private Integer course;

	public FindGroupsAndSubgroupsResult(Level level, String group, Integer subgroups, Integer course) {
		this.level = level;
		this.group = group;
		this.subgroups = subgroups;
		this.course = course;
	}
}
