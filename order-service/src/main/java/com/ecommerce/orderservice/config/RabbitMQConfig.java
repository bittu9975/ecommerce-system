package com.ecommerce.orderservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.order}")
    private String orderExchange;

    @Value("${rabbitmq.queue.order-created}")
    private String orderCreatedQueue;

    @Value("${rabbitmq.queue.order-paid}")
    private String orderPaidQueue;

    @Value("${rabbitmq.queue.payment-request}")
    private String paymentRequestQueue;

    @Value("${rabbitmq.routing-key.order-created}")
    private String orderCreatedRoutingKey;

    @Value("${rabbitmq.routing-key.order-paid}")
    private String orderPaidRoutingKey;

    @Value("${rabbitmq.routing-key.payment-request}")
    private String paymentRequestRoutingKey;

    // Exchange
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(orderExchange);
    }

    // Queues
    @Bean
    public Queue orderCreatedQueue() {
        return new Queue(orderCreatedQueue, true);  // durable = true
    }

    @Bean
    public Queue orderPaidQueue() {
        return new Queue(orderPaidQueue, true);
    }

    @Bean
    public Queue paymentRequestQueue() {
        return new Queue(paymentRequestQueue, true);
    }

    // Bindings
    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder
                .bind(orderCreatedQueue())
                .to(orderExchange())
                .with(orderCreatedRoutingKey);
    }

    @Bean
    public Binding orderPaidBinding() {
        return BindingBuilder
                .bind(orderPaidQueue())
                .to(orderExchange())
                .with(orderPaidRoutingKey);
    }

    @Bean
    public Binding paymentRequestBinding() {
        return BindingBuilder
                .bind(paymentRequestQueue())
                .to(orderExchange())
                .with(paymentRequestRoutingKey);
    }

    // Message Converter (JSON)
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate with JSON converter
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}
