package com.a6raywa1cher.rescheduletsuspring.components.importers;

import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.tsudb", name = "enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class ExternalModelsImportScheduler {
	private final ExternalModelsImporter externalModelsImporter;

	public ExternalModelsImportScheduler(
		@Autowired(required = false) ExternalModelsImporter externalModelsImporter
	) {
		this.externalModelsImporter = externalModelsImporter;
	}

	@Scheduled(cron = "${app.tsudb.cron}")
	public void importExternalModels() {
		try {
			externalModelsImporter.importExternalModels(true);
		} catch (Exception e) {
			log.error("Error during importing", e);
			Sentry.capture(e);
			throw new RuntimeException(e);
		}
	}
}
