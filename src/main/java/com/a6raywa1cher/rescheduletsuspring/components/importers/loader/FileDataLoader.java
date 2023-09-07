package com.a6raywa1cher.rescheduletsuspring.components.importers.loader;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

@Slf4j
public class FileDataLoader implements DataLoader {
	private final Path folder;

	public FileDataLoader(Path folder) {
		this.folder = folder;
	}

	@Override
	public String load(String path, boolean overrideCache) throws IOException {
		try (FileInputStream stream = new FileInputStream(folder.resolve(path).toFile())) {
			byte[] bytes = stream.readAllBytes();
			if (new String(bytes).contains("\ufffd")) {
				return new String(bytes, Charset.forName("windows-1251"));
			} else {
				return new String(bytes);
			}
		}
	}

	@Override
	public void dropCache() throws IOException {
		log.info("Requested to drop cache...");
		FileUtils.cleanDirectory(folder.toFile());
		log.info("Successfully dropped cache!");
	}
}
