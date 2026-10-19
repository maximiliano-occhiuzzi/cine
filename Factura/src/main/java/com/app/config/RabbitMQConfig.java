package com.app.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "venta.eventos";
    public static final String CONFIRMADA_ROUTING_KEY = "venta.confirmada";
    public static final String FACTURA_QUEUE = "cola.factura";

    @Bean
    public Queue facturaQueue() {
        return new Queue(FACTURA_QUEUE, true);
    }

    @Bean
    public TopicExchange ventaExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding facturaBinding(Queue facturaQueue, TopicExchange ventaExchange) {
        return BindingBuilder.bind(facturaQueue).to(ventaExchange).with(CONFIRMADA_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
