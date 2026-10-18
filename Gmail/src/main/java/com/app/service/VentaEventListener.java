package com.app.service;

import com.app.client.AuthClient;
import com.app.config.RabbitMQConfig;
import com.app.dto.EnviarEmailDTO;
import com.app.dto.FacturaItemEventDTO;
import com.app.dto.UsuarioDTO;
import com.app.dto.VentaConfirmadaEvent;
import com.app.model.EmailEnviado;
import com.app.repository.EmailEnviadoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Escucha la cola "cola.confirmacion-venta" (exchange "venta.eventos",
 * routing key "venta.confirmada" — ver RabbitMQConfig): por cada venta
 * confirmada arma el correo de confirmacion con los datos reales de la
 * compra (pelicula, sala, butacas, items, total) y lo manda a traves de
 * EmailService.
 */
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

        // Idempotencia: RabbitMQ es at-least-once, el mismo evento puede llegar
        // mas de una vez (reentrega, retry del publicador). Si esa venta ya se
        // proceso, no se vuelve a mandar el correo.
        if (evento.idVenta() != null && emailEnviadoRepository.existsByVentaId(evento.idVenta())) {
            log.warn("La venta {} ya fue procesada; se ignora el evento duplicado", evento.idVenta());
            return;
        }

        // El evento solo trae el username del comprador (no su mail): hay
        // que resolverlo contra Auth. Si Auth no responde, no hay a donde
        // mandar el correo — se descarta el evento (no es un error del
        // broker, por eso no se tira excepcion: eso reintentaria en loop
        // algo que nunca va a poder resolverse solo).
        String destinatario = resolverEmail(evento.username());
        if (destinatario == null || destinatario.isBlank()) {
            log.warn("No se pudo resolver el mail del usuario {} (venta {}); no se envia el correo",
                    evento.username(), evento.idVenta());
            return;
        }

        String asunto = "Confirmación de tu compra en Cine - " + nullASinDato(evento.numeroCompra());
        String mensaje = armarCuerpo(evento);

        EnviarEmailDTO dto = new EnviarEmailDTO(destinatario, asunto, mensaje);

        // El service manda el mail y persiste el registro; el ventaId se guarda
        // junto con el resto para poder auditar de que compra salio cada correo.
        EmailEnviado registro = emailService.enviar(dto, evento.idVenta());

        log.info("Correo de la venta {} procesado con estado {}",
                registro.getVentaId(), registro.getEstado());
    }

    private String resolverEmail(String username) {
        if (username == null || username.isBlank()) return null;
        try {
            UsuarioDTO usuario = authClient.obtenerUsuario(username);
            return usuario != null ? usuario.email() : null;
        } catch (Exception e) {
            // Cualquier falla de comunicacion con Auth (caido, sin instancia
            // en Eureka, timeout) nunca puede tirar el consumer abajo.
            log.warn("No se pudo resolver el usuario {} contra Auth: {}", username, e.getMessage());
            return null;
        }
    }

    private String armarCuerpo(VentaConfirmadaEvent evento) {
        StringBuilder sb = new StringBuilder();
        String nombre = evento.nombreCompleto();
        sb.append("Hola ").append(nombre != null && !nombre.isBlank() ? nombre : evento.username()).append(",\n\n");
        sb.append("Gracias por tu compra. Resumen:\n");
        if (evento.peliculaTitulo() != null) sb.append("Pelicula: ").append(evento.peliculaTitulo()).append("\n");
        if (evento.salaNombre() != null) {
            sb.append("Sala: ").append(evento.salaNombre());
            if (evento.salaTipo() != null) sb.append(" (").append(evento.salaTipo()).append(")");
            sb.append("\n");
        }
        if (evento.funcionFechaHora() != null) sb.append("Funcion: ").append(evento.funcionFechaHora()).append("\n");
        List<String> butacas = evento.butacas();
        if (butacas != null && !butacas.isEmpty()) {
            sb.append("Butacas: ").append(String.join(", ", butacas)).append("\n");
        }
        sb.append("\n");
        if (evento.entradas() != null && !evento.entradas().isEmpty()) {
            sb.append("Entradas:\n");
            agregarItems(sb, evento.entradas());
        }
        sb.append("Productos de confiteria:\n");
        if (evento.productos() == null || evento.productos().isEmpty()) {
            sb.append("Sin productos de confiteria\n");
        } else {
            agregarItems(sb, evento.productos());
        }
        sb.append("\n");
        if (evento.subtotalEntradas() != null) {
            sb.append("Subtotal entradas: $").append(evento.subtotalEntradas()).append("\n");
        }
        if (evento.subtotalProductos() != null) {
            sb.append("Subtotal productos: $").append(evento.subtotalProductos()).append("\n");
        }
        sb.append("Total: $").append(evento.total() != null ? evento.total() : BigDecimal.ZERO);
        sb.append("\n\nNumero de compra: ").append(nullASinDato(evento.numeroCompra()));
        return sb.toString();
    }

    private void agregarItems(StringBuilder sb, List<FacturaItemEventDTO> items) {
        if (items == null || items.isEmpty()) return;
        items.forEach(i -> {
            sb.append("- ").append(i.nombre()).append(" x").append(i.cantidad());
            if (i.precioUnitario() != null) sb.append(" ($").append(i.precioUnitario()).append(" c/u)");
            sb.append(" = $").append(i.subtotal()).append("\n");
        });
    }

    private String nullASinDato(String valor) {
        return valor != null ? valor : "s/d";
    }
}
