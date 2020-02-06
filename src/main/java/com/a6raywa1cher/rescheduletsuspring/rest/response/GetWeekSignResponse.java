package com.a6raywa1cher.rescheduletsuspring.rest.response;

import com.a6raywa1cher.rescheduletsuspring.models.WeekSign;
import com.a6raywa1cher.rescheduletsuspring.rest.mirror.View;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;

@Data
public class GetWeekSignResponse {
	@JsonView(View.Public.class)
	private WeekSign weekSign;
}
