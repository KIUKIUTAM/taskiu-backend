package com.tavinki.taskiu.rabbitmq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rabbitmq.client.AMQP.Exchange;

@Configuration
public class QueueConfiguration {

    @Bean
    public FanoutExchange fanoutExchange() {
        // ExchangeBuilder.fanoutExchange("taskiu.fanout.exchange").durable(true).build();
        return new FanoutExchange("taskiu.fanout.exchange");
    }

    @Bean
    public Queue taskiuQueue() {
        // QueueBuilder.durable("taskiu.queue").build();
        // QueueBuilder.nondurable("taskiu.queue").build();
        return new Queue("taskiu.queue");
    }

    @Bean
    public Binding fanoutBinding(FanoutExchange fanoutExchange, Queue taskiuQueue) {
        return BindingBuilder.bind(taskiuQueue).to(fanoutExchange);
    }

}
