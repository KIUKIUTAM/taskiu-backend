package com.tavinki.taskiu.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.Exchange;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SpringRabbitListener {

    private static final Logger logger = LoggerFactory.getLogger(SpringRabbitListener.class);

    @RabbitListener(queues = "simple.queue")
    public void listen(String message) {
        logger.info("Received message from RabbitMQ: {}", message);
    }

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "taskiu1.queue", durable = "true"), exchange = @Exchange(value = "taskiu1.fanout.exchange", type = "fanout")))
    public void listenToTaskiuQueue(String message) {
        logger.info("Received message from taskiu1.queue: {}", message);
    }

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "taskiu1.Topic", durable = "true"), exchange = @Exchange(value = "taskiu1.Topic.exchange", type = "topic"), key = "#.news"))
    public void listenToTaskiuTopicQueue(String message) {
        logger.info("Received message from taskiu1.Topic: {}", message);
    }

}
