package com.a6raywa1cher.rescheduletsuspring.components.importers;

public interface ExternalModelsImporter {
	void importExternalModels(boolean overrideCache) throws ImportException;

	boolean isBusy();
}
