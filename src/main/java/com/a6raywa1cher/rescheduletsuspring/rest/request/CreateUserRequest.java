package com.a6raywa1cher.rescheduletsuspring.rest.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class CreateUserRequest {
	@NotBlank
	@Pattern(regexp = "[a-zA-Z0-9]{3,35}")
	private String username;

	@NotBlank
	@Size(max = 1024)
	private String password;
}
