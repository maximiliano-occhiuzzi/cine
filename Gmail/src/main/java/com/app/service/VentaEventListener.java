package com.app.service;

import com.app.config.RabbitMQConfig;
import com.app.dto.EnviarEmailDTO;
import com.app.dto.VentaConfirmadaEvent;
import com.app.model.EmailEnviado;
import com.app.repository.EmailEnviadoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Escucha la cola {@code venta.eventos}: por cada venta confirmada arma el
 * correo de confirmacion y lo manda a traves de {@link EmailService}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VentaEventListener {

    private static final String ASUNTO = "Confirmación de compra - Cine";

    private final EmailService emailService;
    private final EmailEnviadoRepository emailEnviadoRepository;

    @RabbitListener(queues = RabbitMQConfig.COLA_VENTA_EVENTOS)
    public void consumirEventoVenta(VentaConfirmadaEvent evento) {
        log.info("Evento de venta recibido (ventaId={})", evento.ventaId());

        // Sin destinatario no hay a donde mandar el mail; se descarta el evento.
        if (evento.destinatario() == null || evento.destinatario().isBlank()) {
            log.warn("La venta {} no trae destinatario; no se envia el correo", evento.ventaId());
            return;
        }

        // Idempotencia: RabbitMQ es at-least-once, el mismo evento puede llegar
        // mas de una vez (reentrega, retry del publicador). Si esa venta ya se
        // proceso, no se vuelve a mandar el correo.
        if (evento.ventaId() != null && emailEnviadoRepository.existsByVentaId(evento.ventaId())) {
            log.warn("La venta {} ya fue procesada; se ignora el evento duplicado", evento.ventaId());
            return;
        }

        String mensaje = String.format(
                "¡Gracias por tu compra de entradas!\n"
                        + "Película: %s\n"
                        + "Entradas: %s\n"
                        + "Total: $%s",
                evento.pelicula(), evento.cantidad(), evento.montoTotal());

        EnviarEmailDTO dto = new EnviarEmailDTO(evento.destinatario(), ASUNTO, mensaje);

        // El service manda el mail y persiste el registro; el ventaId se guarda
        // junto con el resto para poder auditar de que compra salio cada correo.
        EmailEnviado registro = emailService.enviar(dto, evento.ventaId());

        log.info("Correo de la venta {} procesado con estado {}",
                registro.getVentaId(), registro.getEstado());
    }
}
