package com.a6raywa1cher.rescheduletsuspring.components.tsudbimporter;

import java.io.IOException;

public interface ImportStrategy {
	String load(String path, boolean overrideCache) throws IOException;

	void dropCache() throws IOException;
}
