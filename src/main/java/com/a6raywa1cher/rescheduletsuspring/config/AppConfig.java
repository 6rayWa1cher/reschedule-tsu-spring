package com.a6raywa1cher.rescheduletsuspring.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Component
@ConfigurationProperties(prefix = "app")
@Data
@Validated
public class AppConfig {
	/**
	 * Token for admin features
	 */
	@NotBlank
	private String adminToken;
}
