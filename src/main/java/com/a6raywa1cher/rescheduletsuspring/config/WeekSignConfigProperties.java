package com.a6raywa1cher.rescheduletsuspring.config;

import com.a6raywa1cher.rescheduletsuspring.models.WeekSign;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.week-sign")
@Data
@Validated
public class WeekSignConfigProperties {
	@NotNull
	@Valid
	private WeekSignConfigProperties.FacultyInfo primaryInfo;

	@Valid
	private List<FacultyInfo> additionalInfo = new ArrayList<>();

	@Data
	public final static class FacultyInfo {
		@NotBlank
		private String facultyName;

		@NotNull
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		private LocalDate date;

		@NotNull
		private WeekSign weekSign;

		@Pattern(regexp = "(plusminus|singlemode)")
		private String weekSignStrategy = "plusminus";
	}
}
