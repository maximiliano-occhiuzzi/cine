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

/**
 * Gmail escucha el MISMO exchange/routing key que ya publica MS Ventas para
 * "venta confirmada" (exchange "venta.eventos", routing key
 * "venta.confirmada" — ver RabbitMQConfig de Ventas), con la cola que
 * Ventas ya declara pensada justamente para esto: "cola.confirmacion-venta"
 * (asi la nombro el Figma original: MS Email consume de ahi). Factura
 * escucha el MISMO exchange/routing key pero con su PROPIA cola
 * ("cola.factura") — un exchange topic puede tener varias colas bindeadas
 * a la misma routing key, cada una recibe su copia del mismo mensaje, sin
 * que Ventas tenga que saber que existen las dos.
 *
 * Declarar la misma cola/exchange aca (mismo nombre) es idempotente: si
 * Ventas ya los creo en el broker, esto no los duplica ni los rompe, y deja
 * a Gmail arrancar aunque lo haga antes que Ventas.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "venta.eventos";
    public static final String CONFIRMADA_ROUTING_KEY = "venta.confirmada";
    public static final String COLA_CONFIRMACION_VENTA = "cola.confirmacion-venta";
    public static final String COLA_CONFIRMACION_VENTA_DLQ = "cola.confirmacion-venta.dlq";

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

    /** Cola de descarte: aca caen los eventos que no se pudieron procesar. */
    @Bean
    public Queue ventaConfirmadaDlq() {
        return new Queue(COLA_CONFIRMACION_VENTA_DLQ, true);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Cuando el @RabbitListener agota los reintentos (ver
     * spring.rabbitmq.listener.simple.retry en el yml), en vez de descartar
     * el mensaje o reencolarlo en loop, este recoverer lo republica en la
     * DLQ con headers x-exception-* para poder inspeccionarlo despues.
     * Spring Boot lo toma solo y lo enchufa al interceptor de retry.
     */
    @Bean
    public MessageRecoverer messageRecoverer(RabbitTemplate rabbitTemplate) {
        // exchange "" = exchange por defecto: el routing key es el nombre de la cola.
        return new RepublishMessageRecoverer(rabbitTemplate, "", COLA_CONFIRMACION_VENTA_DLQ);
    }
}
