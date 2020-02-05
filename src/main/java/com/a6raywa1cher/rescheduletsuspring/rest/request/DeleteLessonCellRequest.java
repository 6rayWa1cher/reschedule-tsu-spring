package com.a6raywa1cher.rescheduletsuspring.rest.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class DeleteLessonCellRequest {
	@NotBlank
	@Size(max = 255)
	private String id;
}
