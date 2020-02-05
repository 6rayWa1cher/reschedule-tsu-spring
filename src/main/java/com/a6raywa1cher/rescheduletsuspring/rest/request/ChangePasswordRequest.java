package com.a6raywa1cher.rescheduletsuspring.rest.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ChangePasswordRequest {
	@NotBlank
	private String password;
}
