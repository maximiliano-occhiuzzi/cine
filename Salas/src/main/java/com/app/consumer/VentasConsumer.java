package com.app.consumer;

import com.app.config.RabbitMQConfig;
import com.app.dto.EventoEntradaCompradaDTO;
import com.app.service.ButacaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class VentasConsumer {

    private final ButacaService butacaService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_BUTACAS)
    public void consumirEventoVenta(EventoEntradaCompradaDTO evento) {
        log.info("Evento recibido desde MS-Ventas: Actualizando Butaca ID {} a estado {}",
                evento.butacaId(), evento.nuevoEstado());

        try {
            butacaService.cambiarEstadoButaca(evento.butacaId(), evento.nuevoEstado());
            log.info("Estado de Butaca ID {} actualizado exitosamente", evento.butacaId());
        } catch (Exception e) {
            log.error("Error al procesar actualización de butaca {} desde RabbitMQ, se manda a la dead-letter queue: {}",
                    evento.butacaId(), e.getMessage());
            throw new AmqpRejectAndDontRequeueException(
                    "No se pudo actualizar la butaca " + evento.butacaId(), e);
        }
    }
}