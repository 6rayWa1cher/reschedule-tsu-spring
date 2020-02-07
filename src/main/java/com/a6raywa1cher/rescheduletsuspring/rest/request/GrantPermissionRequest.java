package com.a6raywa1cher.rescheduletsuspring.rest.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class GrantPermissionRequest {
	@NotBlank
	private String username;
	@NotBlank
	@Pattern(regexp = "[а-яА-Яa-zA-Z, \\-0-9()]{3,50}")
	private String faculty;
	@NotBlank
	@Pattern(regexp = "[а-яА-Я, \\-0-9'.(М)]{1,150}")
	private String group;
}
