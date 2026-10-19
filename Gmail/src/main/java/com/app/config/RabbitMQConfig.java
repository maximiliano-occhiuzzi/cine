package com.app.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "venta.eventos";
    public static final String CONFIRMADA_ROUTING_KEY = "venta.confirmada";
    public static final String COLA_CONFIRMACION_VENTA = "cola.confirmacion-venta";
    public static final String COLA_CONFIRMACION_VENTA_DLQ = "cola.confirmacion-venta.dlq";

    public static final String CANCELADA_ROUTING_KEY = "venta.cancelada";
    public static final String COLA_CANCELACION_VENTA = "cola.cancelacion-venta";
    public static final String COLA_CANCELACION_VENTA_DLQ = "cola.cancelacion-venta.dlq";

    @Bean
    public TopicExchange ventaExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue ventaConfirmadaQueue() {
        return new Queue(COLA_CONFIRMACION_VENTA, true);
    }

    @Bean
    public Binding ventaConfirmadaBinding(Queue ventaConfirmadaQueue, TopicExchange ventaExchange) {
        return BindingBuilder.bind(ventaConfirmadaQueue).to(ventaExchange).with(CONFIRMADA_ROUTING_KEY);
    }

    
    @Bean
    public Queue ventaConfirmadaDlq() {
        return new Queue(COLA_CONFIRMACION_VENTA_DLQ, true);
    }

    @Bean
    public Queue ventaCanceladaQueue() {
        return new Queue(COLA_CANCELACION_VENTA, true);
    }

    @Bean
    public Binding ventaCanceladaBinding(Queue ventaCanceladaQueue, TopicExchange ventaExchange) {
        return BindingBuilder.bind(ventaCanceladaQueue).to(ventaExchange).with(CANCELADA_ROUTING_KEY);
    }

    @Bean
    public Queue ventaCanceladaDlq() {
        return new Queue(COLA_CANCELACION_VENTA_DLQ, true);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    
    @Bean
    public MessageRecoverer messageRecoverer(RabbitTemplate rabbitTemplate) {
        return new RepublishMessageRecoverer(rabbitTemplate, "", COLA_CONFIRMACION_VENTA_DLQ);
    }
}
