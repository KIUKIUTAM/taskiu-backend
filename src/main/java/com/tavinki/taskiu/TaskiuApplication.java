package com.tavinki.taskiu;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

import jakarta.annotation.PostConstruct;

@SpringBootApplication(exclude = {
		SecurityAutoConfiguration.class,
		UserDetailsServiceAutoConfiguration.class
})
@EnableMongoAuditing
public class TaskiuApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskiuApplication.class, args);
	}

	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Hong_Kong"));
	}

}
