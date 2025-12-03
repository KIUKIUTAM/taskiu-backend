package com.tavinki.taskiu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tavinki.taskiu.redis.RedisService;

@SpringBootTest
class TaskiuApplicationTests {
	private static final Logger logger = LoggerFactory.getLogger(TaskiuApplicationTests.class);

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
		logger.info(value);
		assertEquals(fakeJson, value);
	}

	@Test
	void testDatabaseConnection() {
		// Check if JdbcTemplate is not null
		assertThat(jdbcTemplate).isNotNull();
		logger.info("JdbcTemplate is properly configured.");
		logger.trace("Testing database connection with a simple query.");
		logger.debug("Executing query: SELECT 1");
		logger.warn("Warning test");
		logger.error("Error test");
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
		logger.info("Sent message to RabbitMQ queue '{}': {}", queueName, testMessage);
		assertThat(1).isEqualTo(1);
	}

}
