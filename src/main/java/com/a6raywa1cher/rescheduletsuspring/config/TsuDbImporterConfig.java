package com.a6raywa1cher.rescheduletsuspring.config;

import com.a6raywa1cher.rescheduletsuspring.components.FileImportStrategy;
import com.a6raywa1cher.rescheduletsuspring.components.ImportStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TsuDbImporterConfig {
	@Bean
	public ImportStrategy importStrategy(TsuDbImporterConfigProperties config) {
		if (config.getImportSource().equals("file")) {
			return new FileImportStrategy(config);
		} else {
			return null;
		}
	}
}
