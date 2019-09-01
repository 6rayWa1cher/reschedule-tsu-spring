package com.a6raywa1cher.rescheduletsuspring.externalmodels;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface IdExternal {
	String url();

	String toSetter();

	Class<?> clazz();
}
