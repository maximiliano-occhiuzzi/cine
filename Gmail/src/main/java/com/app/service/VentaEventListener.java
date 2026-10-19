package com.app.service;

import com.app.client.AuthClient;
import com.app.config.RabbitMQConfig;
import com.app.dto.EnviarEmailDTO;
import com.app.dto.FacturaItemEventDTO;
import com.app.dto.UsuarioDTO;
import com.app.dto.VentaConfirmadaEvent;
import com.app.model.EmailEnviado;
import com.app.repository.EmailEnviadoRepository;
import com.app.util.QrCodeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class VentaEventListener {

    private final EmailService emailService;
    private final EmailEnviadoRepository emailEnviadoRepository;
    private final AuthClient authClient;

    @RabbitListener(queues = RabbitMQConfig.COLA_CONFIRMACION_VENTA)
    public void consumirEventoVenta(VentaConfirmadaEvent evento) {
        log.info("Evento de venta recibido (idVenta={})", evento.idVenta());

        if (evento.idVenta() != null && emailEnviadoRepository.existsByVentaIdAndTipo(evento.idVenta(), "CONFIRMACION")) {
            log.warn("La venta {} ya fue procesada; se ignora el evento duplicado", evento.idVenta());
            return;
        }

        String destinatario = resolverEmail(evento.username());
        if (destinatario == null || destinatario.isBlank()) {
            log.warn("No se pudo resolver el mail del usuario {} (venta {}); no se envia el correo",
                    evento.username(), evento.idVenta());
            return;
        }

        String asunto = "Confirmación de tu compra en Cine - " + nullASinDato(evento.numeroCompra());
        String mensaje = armarCuerpo(evento);

        EnviarEmailDTO dto = new EnviarEmailDTO(destinatario, asunto, mensaje);

        try {
            EmailEnviado registro = emailService.enviar(dto, evento.idVenta());
            log.info("Correo de la venta {} procesado con estado {}",
                    registro.getVentaId(), registro.getEstado());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.warn("La venta {} ya tenia un correo registrado (carrera entre dos entregas del mismo evento); se ignora", evento.idVenta());
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

    private String armarCuerpo(VentaConfirmadaEvent evento) {
        String nombre = evento.nombreCompleto();
        String nombreMostrado = nombre != null && !nombre.isBlank() ? nombre : evento.username();
        String numeroCompra = nullASinDato(evento.numeroCompra());

        String qrBase64 = QrCodeUtil.generarBase64("CINE-TICKET:" + numeroCompra, 220);

        StringBuilder filasDetalle = new StringBuilder();
        if (evento.peliculaTitulo() != null) {
            filasDetalle.append(filaDetalle("Pelicula", evento.peliculaTitulo()));
        }
        if (evento.salaNombre() != null) {
            String sala = evento.salaNombre() + (evento.salaTipo() != null ? " (" + evento.salaTipo() + ")" : "");
            filasDetalle.append(filaDetalle("Sala", sala));
        }
        if (evento.funcionFechaHora() != null) {
            filasDetalle.append(filaDetalle("Funcion", String.valueOf(evento.funcionFechaHora())));
        }
        List<String> butacas = evento.butacas();
        if (butacas != null && !butacas.isEmpty()) {
            filasDetalle.append(filaDetalle("Butacas", String.join(", ", butacas)));
        }

        StringBuilder filasItems = new StringBuilder();
        agregarItems(filasItems, evento.entradas());
        agregarItems(filasItems, evento.productos());

        BigDecimal total = evento.total() != null ? evento.total() : BigDecimal.ZERO;

        String qrHtml = qrBase64 != null
                ? "<img src=\"data:image/png;base64," + qrBase64 + "\" width=\"180\" height=\"180\" "
                    + "alt=\"QR de la entrada\" style=\"display:block;margin:0 auto;border-radius:8px;\" />"
                : "";

        return "<!DOCTYPE html>"
            + "<html><body style=\"margin:0;padding:0;background:" + GRIS_OSCURO + ";font-family:Arial,Helvetica,sans-serif;\">"
            + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:" + GRIS_OSCURO + ";padding:24px 0;\">"
            + "<tr><td align=\"center\">"
            + "<table role=\"presentation\" width=\"420\" cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"background:" + NEGRO + ";border-radius:12px;overflow:hidden;border:1px solid " + GRIS_BORDE + ";\">"

            + "<tr><td style=\"background:" + ROJO + ";padding:18px 24px;\">"
                + "<span style=\"color:" + BLANCO + ";font-size:20px;font-weight:bold;letter-spacing:1px;\">CINE</span>"
                + "<span style=\"color:" + BLANCO + ";font-size:13px;float:right;margin-top:4px;\">Confirmacion de compra</span>"
            + "</td></tr>"

            + "<tr><td style=\"padding:20px 24px 0 24px;\">"
                + "<p style=\"color:" + BLANCO + ";font-size:16px;margin:0 0 4px 0;\">Hola " + nombreMostrado + ",</p>"
                + "<p style=\"color:" + GRIS_TEXTO + ";font-size:13px;margin:0;\">Gracias por tu compra. Aca va el resumen de tu entrada.</p>"
            + "</td></tr>"

            + "<tr><td style=\"padding:16px 24px 0 24px;\">"
                + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" "
                    + "style=\"background:" + GRIS_OSCURO + ";border-radius:8px;\">"
                    + filasDetalle
                + "</table>"
            + "</td></tr>"

            + "<tr><td style=\"padding:16px 24px 0 24px;\">"
                + "<p style=\"color:" + GRIS_TEXTO + ";font-size:11px;text-transform:uppercase;letter-spacing:0.5px;margin:0 0 6px 0;\">Tu compra</p>"
                + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">"
                    + filasItems
                + "</table>"
            + "</td></tr>"

            + "<tr><td style=\"padding:14px 24px 0 24px;\">"
                + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-top:1px solid " + GRIS_BORDE + ";padding-top:10px;\">"
                + "<tr>"
                    + "<td style=\"color:" + BLANCO + ";font-size:15px;font-weight:bold;padding-top:10px;\">Total</td>"
                    + "<td align=\"right\" style=\"color:" + ROJO + ";font-size:18px;font-weight:bold;padding-top:10px;\">$" + total + "</td>"
                + "</tr></table>"
            + "</td></tr>"

            + "<tr><td style=\"padding:22px 24px 0 24px;\" align=\"center\">"
                + "<div style=\"background:" + BLANCO + ";padding:14px;border-radius:12px;display:inline-block;\">"
                    + qrHtml
                + "</div>"
                + "<p style=\"color:" + GRIS_TEXTO + ";font-size:11px;margin:10px 0 0 0;\">Mostra este codigo en boleteria</p>"
                + "<p style=\"color:" + BLANCO + ";font-size:14px;font-weight:bold;letter-spacing:1px;margin:4px 0 0 0;\">" + numeroCompra + "</p>"
            + "</td></tr>"

            + "<tr><td style=\"padding:22px 24px 20px 24px;\">"
                + "<p style=\"color:" + GRIS_TEXTO + ";font-size:11px;text-align:center;margin:0;\">Este es un mail automatico, no respondas a esta direccion.</p>"
            + "</td></tr>"

            + "</table>"
            + "</td></tr></table>"
            + "</body></html>";
    }

    private String filaDetalle(String etiqueta, String valor) {
        return "<tr>"
            + "<td style=\"color:" + GRIS_TEXTO + ";font-size:12px;padding:8px 12px;\">" + etiqueta + "</td>"
            + "<td align=\"right\" style=\"color:" + BLANCO + ";font-size:12px;padding:8px 12px;\">" + valor + "</td>"
            + "</tr>";
    }

    private void agregarItems(StringBuilder sb, List<FacturaItemEventDTO> items) {
        if (items == null) return;
        items.forEach(i -> sb.append("<tr>")
                .append("<td style=\"color:").append(GRIS_TEXTO).append(";font-size:13px;padding:4px 0;\">")
                .append(i.nombre()).append(" x").append(i.cantidad()).append("</td>")
                .append("<td align=\"right\" style=\"color:").append(BLANCO).append(";font-size:13px;padding:4px 0;\">")
                .append("$").append(i.subtotal()).append("</td>")
                .append("</tr>"));
    }

    private String nullASinDato(String valor) {
        return valor != null ? valor : "s/d";
    }
}
