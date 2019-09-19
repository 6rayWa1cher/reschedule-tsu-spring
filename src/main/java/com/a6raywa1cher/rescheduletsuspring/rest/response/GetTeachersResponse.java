package com.a6raywa1cher.rescheduletsuspring.rest.response;

import lombok.Data;

import java.util.List;

@Data
public class GetTeachersResponse {
	private List<String> teachers;
}
