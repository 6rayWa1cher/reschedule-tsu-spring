package com.a6raywa1cher.rescheduletsuspring.rest.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class ChangePasswordRequest {
	@NotBlank
	@Size(max = 1024)
	private String password;
}
