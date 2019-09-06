package com.a6raywa1cher.rescheduletsuspring.components.weeksign;

import com.a6raywa1cher.rescheduletsuspring.config.WeekSignConfigProperties;
import com.a6raywa1cher.rescheduletsuspring.models.WeekSign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

@Component
public class WeekSignComponent {
	private WeekSignConfigProperties properties;

	@Autowired
	public WeekSignComponent(WeekSignConfigProperties properties) {
		this.properties = properties;
	}

	private WeekSignStrategy toWeekSignStrategy(String strategyName) {
		switch (strategyName) {
			case "plusminus":
				return new PlusMinusWeekSignStrategy();
			case "singlemode":
				return new SingleModeWeekSignStrategy();
			default:
				throw new RuntimeException(String.format("Not found strategy for name %s", strategyName));
		}
	}

	public WeekSign getWeekSign(Date date, String faculty) {
		Optional<WeekSignConfigProperties.FacultyInfo> optionalFacultyInfo = properties.getAdditionalInfo().stream()
			.filter(facultyInfo -> facultyInfo.getFacultyName().equals(faculty))
			.findAny();
		WeekSignConfigProperties.FacultyInfo facultyInfo = optionalFacultyInfo.orElse(properties.getPrimaryInfo());
		return toWeekSignStrategy(facultyInfo.getWeekSignStrategy()).getWeekSign(date, facultyInfo.getDate(), facultyInfo.getWeekSign());
	}
}
