package com.a6raywa1cher.rescheduletsuspring.service.submodels;

import com.a6raywa1cher.rescheduletsuspring.models.Level;
import lombok.Data;

@Data
public class GroupInfo {
	private String faculty;
	private Level level;
	private String name;
	private Integer subgroups;
	private Integer course;
}
