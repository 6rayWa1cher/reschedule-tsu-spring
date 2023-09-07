package com.a6raywa1cher.rescheduletsuspring.components.importers.tsutiming.models;

import lombok.Data;

import java.util.List;

@Data
public class TimeSchedule {
	private String _id;

	private List<String> schedule; // list of strings of lectures times like "12:10-13:45"

	private String name; // looks like the flag of default or temporary schedule
}
