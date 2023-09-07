package com.a6raywa1cher.rescheduletsuspring.components.importers.loader;

import java.io.IOException;

public interface DataLoader {
	String load(String path, boolean overrideCache) throws IOException;

	void dropCache() throws IOException;
}
