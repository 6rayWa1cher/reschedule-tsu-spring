package com.a6raywa1cher.rescheduletsuspring.components.importers;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractExternalModelsImporter implements ExternalModelsImporter {
	private final AtomicBoolean invoked = new AtomicBoolean();

	@Override
	public void importExternalModels(boolean overrideCache) throws ImportException {
		if (!invoked.compareAndSet(false, true)) {
			throw new ImportException("Already executing");
		}
		try {
			internalImportExternalModels(overrideCache);
		} finally {
			invoked.set(false);
		}
	}

	protected abstract void internalImportExternalModels(boolean overrideCache) throws ImportException;


	@Override
	public boolean isBusy() {
		return invoked.get();
	}
}
