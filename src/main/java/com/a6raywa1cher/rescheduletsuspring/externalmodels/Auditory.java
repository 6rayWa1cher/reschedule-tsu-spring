package com.a6raywa1cher.rescheduletsuspring.externalmodels;

import lombok.Data;

@Data
public class Auditory {
	private String _id;
	private String name; // usually, number of auditory. primary info
	private String extraName; // additional info
	private String housing; // building id
	private int capacity; // unused
	private int used; // ?
	private boolean computer; // unused
	private boolean projector; // unused
	private int __v;
}
