package com.a6raywa1cher.rescheduletsuspring.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "app")
@Data
@Validated
public class AppConfigProperties {
	private String host;
}
