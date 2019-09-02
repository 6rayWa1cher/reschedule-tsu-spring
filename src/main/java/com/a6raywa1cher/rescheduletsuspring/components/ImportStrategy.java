package com.a6raywa1cher.rescheduletsuspring.components;

import java.io.IOException;

public interface ImportStrategy {
	String load(String path, boolean overrideCache) throws IOException;
}
