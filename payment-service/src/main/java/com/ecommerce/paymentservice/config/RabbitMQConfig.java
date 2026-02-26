package com.ecommerce.paymentservice.config;

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

    @Value("${rabbitmq.queue.payment-request}")
    private String paymentRequestQueue;

    @Value("${rabbitmq.queue.order-paid}")
    private String orderPaidQueue;

    @Value("${rabbitmq.routing-key.payment-request}")
    private String paymentRequestRoutingKey;

    @Value("${rabbitmq.routing-key.order-paid}")
    private String orderPaidRoutingKey;

    @Value("${rabbitmq.queue.payment-success}")
    private String paymentSuccessQueue;

    @Value("${rabbitmq.queue.payment-failed}")
    private String paymentFailedQueue;

    @Value("${rabbitmq.routing-key.payment-success}")
    private String paymentSuccessRoutingKey;

    @Value("${rabbitmq.routing-key.payment-failed}")
    private String paymentFailedRoutingKey;

    // Exchange
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(orderExchange);
    }

    // Queues
    @Bean
    public Queue paymentRequestQueue() {
        return new Queue(paymentRequestQueue, true);
    }

    @Bean
    public Queue orderPaidQueue() {
        return new Queue(orderPaidQueue, true);
    }

    // Bindings
    @Bean
    public Binding paymentRequestBinding() {
        return BindingBuilder
                .bind(paymentRequestQueue())
                .to(orderExchange())
                .with(paymentRequestRoutingKey);
    }

    @Bean
    public Binding orderPaidBinding() {
        return BindingBuilder
                .bind(orderPaidQueue())
                .to(orderExchange())
                .with(orderPaidRoutingKey);
    }

    // Message Converter
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

    @Bean
    public Queue paymentSuccessQueue() {
        return new Queue(paymentSuccessQueue, true);
    }

    @Bean
    public Queue paymentFailedQueue() {
        return new Queue(paymentFailedQueue, true);
    }

    @Bean
    public Binding paymentSuccessBinding() {
        return BindingBuilder.bind(paymentSuccessQueue())
                .to(orderExchange()).with(paymentSuccessRoutingKey);
    }

    @Bean
    public Binding paymentFailedBinding() {
        return BindingBuilder.bind(paymentFailedQueue())
                .to(orderExchange()).with(paymentFailedRoutingKey);
    }
}
