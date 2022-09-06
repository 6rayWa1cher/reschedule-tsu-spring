package com.a6raywa1cher.rescheduletsuspring.config;

import com.a6raywa1cher.rescheduletsuspring.components.tsudbimporter.FileImportStrategy;
import com.a6raywa1cher.rescheduletsuspring.components.tsudbimporter.ImportStrategy;
import com.a6raywa1cher.rescheduletsuspring.components.tsudbimporter.NetworkImportStrategy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.nio.file.Path;

@Configuration
public class TsuDbImporterConfig {
	@Bean
	@ConditionalOnProperty(prefix = "app.tsudb", name = "enabled", havingValue = "true", matchIfMissing = true)
	public ImportStrategy importStrategy(TsuDbImporterConfigProperties config, RestTemplateBuilder builder) {
		if (config.getImportSource().equals("file")) {
			return new FileImportStrategy(Path.of(config.getCachePath()));
		} else {
			return new NetworkImportStrategy(builder, URI.create(config.getPath()), Path.of(config.getCachePath()));
		}
	}
}
