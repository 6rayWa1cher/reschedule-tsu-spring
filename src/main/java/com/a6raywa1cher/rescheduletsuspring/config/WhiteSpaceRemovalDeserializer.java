package com.a6raywa1cher.rescheduletsuspring.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.SneakyThrows;

// https://stackoverflow.com/questions/6852213/can-jackson-be-configured-to-trim-leading-trailing-whitespace-from-all-string-pr
public class WhiteSpaceRemovalDeserializer extends JsonDeserializer<String> {
	@SneakyThrows
	@Override
	public String deserialize(JsonParser jp, DeserializationContext ctx) {
		String valueAsString = jp.getValueAsString();
		return valueAsString == null ? null : valueAsString.strip();
	}
}