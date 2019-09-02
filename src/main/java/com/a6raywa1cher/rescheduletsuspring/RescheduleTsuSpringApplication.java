package com.a6raywa1cher.rescheduletsuspring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RescheduleTsuSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(RescheduleTsuSpringApplication.class, args);
	}

}
