package com.app.service;

import com.app.client.AuthClient;
import com.app.config.RabbitMQConfig;
import com.app.dto.EnviarEmailDTO;
import com.app.dto.UsuarioDTO;
import com.app.dto.VentaCanceladaEvent;
import com.app.model.EmailEnviado;
import com.app.repository.EmailEnviadoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;


@Slf4j
@Component
@RequiredArgsConstructor
public class VentaCanceladaEventListener {

    private final EmailService emailService;
    private final EmailEnviadoRepository emailEnviadoRepository;
    private final AuthClient authClient;

    private static final String TIPO = "CANCELACION";

    @RabbitListener(queues = RabbitMQConfig.COLA_CANCELACION_VENTA)
    public void consumirVentaCancelada(VentaCanceladaEvent evento) {
        log.info("Evento de venta cancelada recibido (idVenta={})", evento.idVenta());

        if (evento.idVenta() != null && emailEnviadoRepository.existsByVentaIdAndTipo(evento.idVenta(), TIPO)) {
            log.warn("La cancelacion de la venta {} ya fue procesada; se ignora el evento duplicado", evento.idVenta());
            return;
        }

        String destinatario = resolverEmail(evento.username());
        if (destinatario == null || destinatario.isBlank()) {
            log.warn("No se pudo resolver el mail del usuario {} (venta {} cancelada); no se envia el correo",
                    evento.username(), evento.idVenta());
            return;
        }

        String asunto = "Cancelación de tu compra en Cine - " + nullASinDato(evento.numeroCompra());
        String mensaje = armarCuerpo(evento);
        EnviarEmailDTO dto = new EnviarEmailDTO(destinatario, asunto, mensaje);

        try {
            EmailEnviado registro = emailService.enviar(dto, evento.idVenta(), TIPO);
            log.info("Correo de cancelacion de la venta {} procesado con estado {}",
                    registro.getVentaId(), registro.getEstado());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.warn("La cancelacion de la venta {} ya tenia un correo registrado (carrera entre dos entregas del mismo evento); se ignora", evento.idVenta());
        }
    }

    private String resolverEmail(String username) {
        if (username == null || username.isBlank()) return null;
        try {
            UsuarioDTO usuario = authClient.obtenerUsuario(username);
            return usuario != null ? usuario.email() : null;
        } catch (Exception e) {
            log.warn("No se pudo resolver el usuario {} contra Auth: {}", username, e.getMessage());
            return null;
        }
    }

    private static final String ROJO = "#e50914";
    private static final String NEGRO = "#141414";
    private static final String GRIS_OSCURO = "#1f1f1f";
    private static final String GRIS_BORDE = "#333333";
    private static final String GRIS_TEXTO = "#bdbdbd";
    private static final String BLANCO = "#ffffff";
    private static final String VERDE = "#a5d6a7";
    private static final String AMARILLO = "#ffcc80";

    private String armarCuerpo(VentaCanceladaEvent evento) {
        String nombre = evento.nombreCompleto();
        String nombreMostrado = nombre != null && !nombre.isBlank() ? nombre : evento.username();
        String numeroCompra = nullASinDato(evento.numeroCompra());

        String detallePelicula = "";
        if (evento.peliculaTitulo() != null) {
            detallePelicula += filaDetalle("Película", evento.peliculaTitulo());
        }
        if (evento.butacas() != null && !evento.butacas().isEmpty()) {
            detallePelicula += filaDetalle("Butacas", String.join(", ", evento.butacas()));
        }

        BigDecimal total = evento.total() != null ? evento.total() : BigDecimal.ZERO;
        String mensajeReembolso = mensajeReembolso(evento.estadoReembolso());

        return "<!DOCTYPE html>"
            + "<html><body style=\"margin:0;padding:0;background:" + GRIS_OSCURO + ";font-family:Arial,Helvetica,sans-serif;\">"
            + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:" + GRIS_OSCURO + ";padding:24px 0;\">"
            + "<tr><td align=\"center\">"
            + "<table role=\"presentation\" width=\"420\" cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"background:" + NEGRO + ";border-radius:12px;overflow:hidden;border:1px solid " + GRIS_BORDE + ";\">"

            + "<tr><td style=\"background:" + ROJO + ";padding:18px 24px;\">"
                + "<span style=\"color:" + BLANCO + ";font-size:20px;font-weight:bold;letter-spacing:1px;\">CINE</span>"
                + "<span style=\"color:" + BLANCO + ";font-size:13px;float:right;margin-top:4px;\">Compra cancelada</span>"
            + "</td></tr>"

            + "<tr><td style=\"padding:20px 24px 0 24px;\">"
                + "<p style=\"color:" + BLANCO + ";font-size:16px;margin:0 0 4px 0;\">Hola " + nombreMostrado + ",</p>"
                + "<p style=\"color:" + GRIS_TEXTO + ";font-size:13px;margin:0;\">Tu compra <strong>" + numeroCompra + "</strong> fue cancelada. Las butacas quedaron liberadas.</p>"
            + "</td></tr>"

            + "<tr><td style=\"padding:16px 24px 0 24px;\">"
                + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" "
                    + "style=\"background:" + GRIS_OSCURO + ";border-radius:8px;\">"
                    + detallePelicula
                    + filaDetalle("Monto de la compra", "$" + total)
                + "</table>"
            + "</td></tr>"

            + "<tr><td style=\"padding:18px 24px 0 24px;\">"
                + mensajeReembolso
            + "</td></tr>"

            + "<tr><td style=\"padding:22px 24px 20px 24px;\">"
                + "<p style=\"color:" + GRIS_TEXTO + ";font-size:11px;text-align:center;margin:0;\">Este es un mail automatico, no respondas a esta direccion.</p>"
            + "</td></tr>"

            + "</table>"
            + "</td></tr></table>"
            + "</body></html>";
    }

    private String mensajeReembolso(String estadoReembolso) {
        if ("COMPLETADO".equals(estadoReembolso)) {
            return "<p style=\"color:" + VERDE + ";font-size:13px;margin:0;\">✔ El reembolso ya fue procesado por Mercado Pago.</p>";
        }
        if ("FALLIDO".equals(estadoReembolso)) {
            return "<p style=\"color:" + AMARILLO + ";font-size:13px;margin:0;\">El pago ya estaba acreditado y el reembolso automático no se pudo completar. "
                    + "Nuestro equipo lo va a procesar manualmente a la brevedad.</p>";
        }
        return "<p style=\"color:" + GRIS_TEXTO + ";font-size:13px;margin:0;\">Esta compra no llegó a pagarse, así que no hay nada que reembolsar.</p>";
    }

    private String filaDetalle(String etiqueta, String valor) {
        return "<tr>"
            + "<td style=\"color:" + GRIS_TEXTO + ";font-size:12px;padding:8px 12px;\">" + etiqueta + "</td>"
            + "<td align=\"right\" style=\"color:" + BLANCO + ";font-size:12px;padding:8px 12px;\">" + valor + "</td>"
            + "</tr>";
    }

    private String nullASinDato(String valor) {
        return valor != null ? valor : "s/d";
    }
}
