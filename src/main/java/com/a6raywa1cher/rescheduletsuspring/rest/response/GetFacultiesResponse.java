package com.a6raywa1cher.rescheduletsuspring.rest.response;

import com.a6raywa1cher.rescheduletsuspring.rest.mirror.View;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;

import java.util.List;

@Data
public class GetFacultiesResponse {
	@JsonView(View.Public.class)
	private List<String> faculties;
}
