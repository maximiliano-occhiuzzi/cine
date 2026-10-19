package com.app.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "venta.eventos";

    public static final String CONFIRMADA_QUEUE = "cola.confirmacion-venta";
    public static final String CONFIRMADA_ROUTING_KEY = "venta.confirmada";

    public static final String CANCELADA_QUEUE = "cola.cancelacion-venta";
    public static final String CANCELADA_ROUTING_KEY = "venta.cancelada";

    public static final String EXCHANGE_VENTAS_SALA = "exchange.ventas";
    public static final String ROUTING_KEY_BUTACAS = "routing.butacas";

    @Bean
    public DirectExchange exchangeVentasSala() {
        return new DirectExchange(EXCHANGE_VENTAS_SALA);
    }

    @Bean
    public Queue ventaConfirmadaQueue() {
        return new Queue(CONFIRMADA_QUEUE, true);
    }

    @Bean
    public Queue ventaCanceladaQueue() {
        return new Queue(CANCELADA_QUEUE, true);
    }

    @Bean
    public TopicExchange ventaExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding ventaConfirmadaBinding(Queue ventaConfirmadaQueue, TopicExchange ventaExchange) {
        return BindingBuilder.bind(ventaConfirmadaQueue).to(ventaExchange).with(CONFIRMADA_ROUTING_KEY);
    }

    @Bean
    public Binding ventaCanceladaBinding(Queue ventaCanceladaQueue, TopicExchange ventaExchange) {
        return BindingBuilder.bind(ventaCanceladaQueue).to(ventaExchange).with(CANCELADA_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
