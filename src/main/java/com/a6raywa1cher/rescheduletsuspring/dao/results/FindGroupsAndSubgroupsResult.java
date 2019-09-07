package com.a6raywa1cher.rescheduletsuspring.dao.results;

import com.a6raywa1cher.rescheduletsuspring.externalmodels.Direction;
import lombok.Data;

@Data
public class FindGroupsAndSubgroupsResult {
	private Direction.Level level;

	private String group;

	private Integer subgroups;

	private Integer course;

	public FindGroupsAndSubgroupsResult(Direction.Level level, String group, Integer subgroups, Integer course) {
		this.level = level;
		this.group = group;
		this.subgroups = subgroups;
		this.course = course;
	}
}
