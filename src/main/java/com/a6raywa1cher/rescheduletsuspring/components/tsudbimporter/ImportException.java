package com.a6raywa1cher.rescheduletsuspring.components.tsudbimporter;

public class ImportException extends Exception {
	public ImportException(String message) {
		super(message);
	}

	public ImportException(String message, Throwable cause) {
		super(message, cause);
	}
}
