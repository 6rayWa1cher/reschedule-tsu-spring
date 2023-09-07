package com.a6raywa1cher.rescheduletsuspring.components.importers.tsutiming.models;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface IdExternal {
	String url();

	String toSetter();

	Class<?> clazz();

	boolean mutable() default false;
}
