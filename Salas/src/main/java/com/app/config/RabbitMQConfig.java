package com.app.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_BUTACAS = "queue.butacas.actualizar";
    public static final String EXCHANGE_VENTAS = "exchange.ventas";
    public static final String ROUTING_KEY_BUTACAS = "routing.butacas";

    public static final String DLX = "exchange.ventas.dlx";
    public static final String QUEUE_BUTACAS_DLQ = "queue.butacas.actualizar.dlq";

    @Bean
    public Queue queueButacas() {
        return new Queue(QUEUE_BUTACAS, true, false, false,
                Map.of("x-dead-letter-exchange", DLX, "x-dead-letter-routing-key", ROUTING_KEY_BUTACAS));
    }

    @Bean
    public DirectExchange exchangeVentas() {
        return new DirectExchange(EXCHANGE_VENTAS);
    }

    @Bean
    public Binding bindingButacas(Queue queueButacas, DirectExchange exchangeVentas) {
        return BindingBuilder.bind(queueButacas).to(exchangeVentas).with(ROUTING_KEY_BUTACAS);
    }

    @Bean
    public DirectExchange exchangeVentasDlx() {
        return new DirectExchange(DLX);
    }

    @Bean
    public Queue queueButacasDlq() {
        return new Queue(QUEUE_BUTACAS_DLQ, true);
    }

    @Bean
    public Binding bindingButacasDlq(Queue queueButacasDlq, DirectExchange exchangeVentasDlx) {
        return BindingBuilder.bind(queueButacasDlq).to(exchangeVentasDlx).with(ROUTING_KEY_BUTACAS);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}