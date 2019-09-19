package com.a6raywa1cher.rescheduletsuspring.service.submodels;

import com.a6raywa1cher.rescheduletsuspring.externalmodels.Direction;
import lombok.Data;

@Data
public class GroupInfo {
	private String faculty;
	private Direction.Level level;
	private String name;
	private Integer subgroups;
	private Integer course;
}
