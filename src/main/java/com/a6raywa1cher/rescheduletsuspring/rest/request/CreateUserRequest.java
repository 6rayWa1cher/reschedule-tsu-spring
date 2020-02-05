package com.a6raywa1cher.rescheduletsuspring.rest.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class CreateUserRequest {
	@NotBlank
	@Pattern(regexp = "[a-zA-Z0-9]{3,35}")
	private String username;

	@NotBlank
	private String password;
}
