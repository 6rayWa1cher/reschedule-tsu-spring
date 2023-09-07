package com.a6raywa1cher.rescheduletsuspring.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Component
@ConfigurationProperties(prefix = "app.tsudb")
@Validated
@Data
public class TsuDbImporterConfigProperties {
	/**
	 * Is Tsu database importer must be enabled
	 */
	private boolean enabled = true;

	@NotBlank
	private String path;

	/**
	 * Import source (timetable or timing)
	 */
	@Pattern(regexp = "(timetable|timing)")
	private String remoteType;

	/**
	 * Import source strategy (file or network)
	 */
	@NotBlank
	@Pattern(regexp = "(file|network)")
	private String importSource;
	/**
	 * Path to cache (empty if chosen file strategy)
	 */
	private String cachePath;

	/**
	 * Delay for importer
	 */
	private String cron;

	/**
	 * Filter for seasons
	 */
	@NotBlank
	private String currentSeason;

	/**
	 * Filter for semesters
	 */
	@NotBlank
	private String semester;
}
