package com.tavinki.taskiu;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tavinki.taskiu.common.test.controller.RedisService;

@SpringBootTest
class TaskiuApplicationTests {
	private static final Logger customLogger = LoggerFactory.getLogger(TaskiuApplicationTests.class);

	@Autowired
	RedisService redisService;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void contextLoads() {
		String fakeJson = """
				{
				  "user": {
				    "id": 12345,
				    "name": "Alice Chen",
				    "email": "alice.chen@example.com",
				    "age": 28,
				    "isActive": true,
				    "roles": ["admin", "editor"],
				    "profile": {
				      "avatar": "https://example.com/avatar/alice.jpg",
				      "bio": "Coffee lover. Software developer. Traveler."
				    },
				    "createdAt": "2025-10-27T15:10:00Z"
				  }
				}
				""" //
		;
		redisService.save("testKey", fakeJson);
		String value = (String) redisService.get("testKey");
		customLogger.info(value);
		assertEquals(fakeJson, value);
	}

	@Test
	void testDatabaseConnection() {
		// Check if JdbcTemplate is not null
		assertThat(jdbcTemplate).isNotNull();
		customLogger.info("JdbcTemplate is properly configured.");
		customLogger.trace("Testing database connection with a simple query.");
		customLogger.debug("Executing query: SELECT 1");
		customLogger.warn("Warning test");
		customLogger.error("Error test");
		// Run a simple query to test the connection
		Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
		assertThat(result).isEqualTo(1);
	}

	@Test
	void testRabbitMQSend() {
		// This test will simply check if the application context loads with RabbitMQ
		// dependencies
		String queueName = "simple.queue";
		String testMessage = "Hello, RabbitMQ!";

		rabbitTemplate.convertAndSend(queueName, testMessage);
		customLogger.info("Sent message to RabbitMQ queue '{}': {}", queueName, testMessage);
		assertThat(1).isEqualTo(1);
	}

	// @Test
	// void testMongoDBConnection() {
	// // This test will simply check if the application context loads with MongoDB
	// // dependencies
	// User user = User.builder().email("test@test.com").name("Test
	// User").picture(null).build();
	// userService.createUser(user);
	// userService.getUserByEmail("test1@test.com");
	// assertThat(1).isEqualTo(1);
	// customLogger.info("MongoDB dependencies are properly configured.");
	// }

	@Test
	void testTimeUzoneSetting() {
		// Verify that the default timezone is set to Asia/Hong_Kong
		String expectedTimeZone = "Asia/Hong_Kong";
		String actualTimeZone = java.util.TimeZone.getDefault().getID();
		assertEquals(expectedTimeZone, actualTimeZone);
		customLogger.info("Default timezone is correctly set to {}", actualTimeZone);
	}

}
