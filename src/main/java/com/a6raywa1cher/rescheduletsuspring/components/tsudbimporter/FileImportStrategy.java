package com.a6raywa1cher.rescheduletsuspring.components.tsudbimporter;

import com.a6raywa1cher.rescheduletsuspring.config.TsuDbImporterConfigProperties;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

public class FileImportStrategy implements ImportStrategy {
	private TsuDbImporterConfigProperties properties;

	public FileImportStrategy(TsuDbImporterConfigProperties properties) {
		this.properties = properties;
	}

	@Override
	public String load(String path, boolean overrideCache) throws IOException {
		try (FileInputStream stream = new FileInputStream(Path.of(properties.getPath(), path).toFile())) {
			byte[] bytes = stream.readAllBytes();
			if (new String(bytes).contains("\ufffd")) {
				return new String(bytes, Charset.forName("windows-1251"));
			} else {
				return new String(bytes);
			}
		}
	}
}
