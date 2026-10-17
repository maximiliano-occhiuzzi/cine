package com.app.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion de RabbitMQ del modulo Gmail.
 *
 * <p>Declara la cola de la que llegan los eventos de venta, su cola de descarte
 * (DLQ) y el converter JSON para que los mensajes se transformen directo a
 * objetos Java.</p>
 */
@Configuration
public class RabbitMQConfig {

    /** Cola donde MS Ventas publica las ventas confirmadas. */
    public static final String COLA_VENTA_EVENTOS = "venta.eventos";

    /** Cola de descarte: aca caen los eventos que no se pudieron procesar. */
    public static final String COLA_VENTA_EVENTOS_DLQ = "venta.eventos.dlq";

    /**
     * Cola durable: sobrevive a un reinicio del broker. MS Ventas tiene que
     * declararla con la misma configuracion.
     */
    @Bean
    public Queue ventaEventosQueue() {
        return new Queue(COLA_VENTA_EVENTOS, true);
    }

    /** Cola durable donde se guardan los eventos que fallaron. */
    @Bean
    public Queue ventaEventosDlq() {
        return new Queue(COLA_VENTA_EVENTOS_DLQ, true);
    }

    /**
     * Converter JSON (Jackson 3). Spring Boot lo toma automaticamente para el
     * listener, asi que el parametro del @RabbitListener se deserializa solo.
     */
    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    /**
     * Cuando el @RabbitListener agota los reintentos, en vez de descartar el
     * mensaje o reencolarlo en loop, este recoverer lo republica en la DLQ con
     * headers {@code x-exception-*} para poder inspeccionarlo despues.
     *
     * <p>Spring Boot toma este bean solo y lo enchufa al interceptor de retry
     * (siempre que {@code spring.rabbitmq.listener.simple.retry.enabled=true}).</p>
     */
    @Bean
    public MessageRecoverer messageRecoverer(RabbitTemplate rabbitTemplate) {
        // exchange "" = exchange por defecto: el routing key es el nombre de la cola.
        return new RepublishMessageRecoverer(rabbitTemplate, "", COLA_VENTA_EVENTOS_DLQ);
    }
}
