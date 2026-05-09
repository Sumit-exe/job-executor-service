package com.airtribe.jobexecutor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
public class JobexecutorApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobexecutorApplication.class, args);
	}

}
