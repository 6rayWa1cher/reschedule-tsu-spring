package com.a6raywa1cher.rescheduletsuspring.components.tsudbimporter;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public class NetworkImportStrategy implements ImportStrategy {
	private static final Logger log = LoggerFactory.getLogger(NetworkImportStrategy.class);
	private RestTemplate restTemplate;
	private URI networkPath;
	private Path cachePath;

	public NetworkImportStrategy(RestTemplateBuilder restTemplateBuilder, URI networkPath, Path cachePath) {
		restTemplate = restTemplateBuilder.build();
		this.networkPath = networkPath;
		this.cachePath = cachePath;
	}

	@Override
	public String load(String path, boolean overrideCache) throws IOException {
		Path cache = cachePath.resolve(path);
		if (!cache.toFile().getParentFile().exists()) {
			Files.createDirectories(cache.toFile().getParentFile().toPath());
		}
		if (!cache.toFile().exists() || overrideCache) {
			URI uri = networkPath.resolve(path);
			log.info("Requesting {}", uri);
			ResponseEntity<String> responseEntity = restTemplate.getForEntity(
				uri, String.class);
			if (!responseEntity.getStatusCode().is2xxSuccessful()) {
				throw new IOException(String.format("Wrong result. Path: %s, Code:%d, content:%s",
					path, responseEntity.getStatusCodeValue(), responseEntity.getBody()));
			}
			try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(cache.toFile()))) {
				writer.append(responseEntity.getBody());
			}
		}
		try (FileInputStream stream = new FileInputStream(cache.toFile())) {
			byte[] bytes = stream.readAllBytes();
			if (new String(bytes).contains("\ufffd")) { // which means that russian language corrupted due to windows1251
				return new String(bytes, Charset.forName("windows-1251"));
			} else {
				return new String(bytes);
			}
		}
	}

	@Override
	public void dropCache() throws IOException {
		log.info("Requested to drop cache...");
		FileUtils.cleanDirectory(cachePath.toFile());
		log.info("Successfully dropped cache!");
	}
}
