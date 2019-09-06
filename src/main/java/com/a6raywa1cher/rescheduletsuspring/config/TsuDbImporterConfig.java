package com.a6raywa1cher.rescheduletsuspring.config;

import com.a6raywa1cher.rescheduletsuspring.components.tsudbimporter.FileImportStrategy;
import com.a6raywa1cher.rescheduletsuspring.components.tsudbimporter.ImportStrategy;
import com.a6raywa1cher.rescheduletsuspring.components.tsudbimporter.NetworkImportStrategy;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TsuDbImporterConfig {
	@Bean
	public ImportStrategy importStrategy(TsuDbImporterConfigProperties config, RestTemplateBuilder builder) {
		if (config.getImportSource().equals("file")) {
			return new FileImportStrategy(config);
		} else {
			return new NetworkImportStrategy(builder, config);
		}
	}
}
