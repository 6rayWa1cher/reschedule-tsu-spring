package com.a6raywa1cher.rescheduletsuspring.config;

import com.a6raywa1cher.rescheduletsuspring.components.importers.loader.DataLoader;
import com.a6raywa1cher.rescheduletsuspring.components.importers.loader.FileDataLoader;
import com.a6raywa1cher.rescheduletsuspring.components.importers.loader.NetworkDataLoader;
import com.a6raywa1cher.rescheduletsuspring.config.properties.TsuDbImporterConfigProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.nio.file.Path;

@Configuration
@ConditionalOnProperty(prefix = "app.tsudb", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TsuDbImporterConfig {
	@Bean
	public DataLoader importStrategy(TsuDbImporterConfigProperties config, RestTemplateBuilder builder) {
		if (config.getImportSource().equals("file")) {
			return new FileDataLoader(Path.of(config.getCachePath()));
		} else {
			return new NetworkDataLoader(builder, URI.create(config.getPath()), Path.of(config.getCachePath()));
		}
	}
}
