package com.a6raywa1cher.rescheduletsuspring.externalmodels;

import lombok.Data;

@Data
public class Faculty {
	private int __v;
	private String _id;
	private String abbr; // short
	private String favouriteHousing; // looks like building
	private String name; // full
	private int used; // ?

	private String headName;
	private String headTitle;
}
