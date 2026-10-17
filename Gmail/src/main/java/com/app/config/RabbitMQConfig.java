package com.app.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion de RabbitMQ del modulo Gmail.
 *
 * <p>Declara la cola de la que llegan los eventos de venta y el converter JSON
 * para que los mensajes se transformen directo a objetos Java.</p>
 */
@Configuration
public class RabbitMQConfig {

    /** Cola donde MS Ventas publica las ventas confirmadas. */
    public static final String COLA_VENTA_EVENTOS = "venta.eventos";

    /**
     * Cola durable: sobrevive a un reinicio del broker. MS Ventas tiene que
     * declararla con la misma configuracion.
     */
    @Bean
    public Queue ventaEventosQueue() {
        return new Queue(COLA_VENTA_EVENTOS, true);
    }

    /**
     * Converter JSON (Jackson 3). Spring Boot lo toma automaticamente para el
     * listener, asi que el parametro del @RabbitListener se deserializa solo.
     */
    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
