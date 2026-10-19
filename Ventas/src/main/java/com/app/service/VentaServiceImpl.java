package com.app.service;

import com.app.client.AuthClient;
import com.app.client.MercadoPagoClient;
import com.app.client.ProductoClient;
import com.app.client.SalaClient;
import com.app.config.RabbitMQConfig;
import com.app.dto.AjustarStockDTO;
import com.app.dto.ButacaDTO;
import com.app.dto.FuncionDTO;
import com.app.dto.MercadoPagoPaymentResponse;
import com.app.dto.MercadoPagoPreferenceRequest;
import com.app.dto.MercadoPagoPreferenceResponse;
import com.app.dto.MercadoPagoRefundResponse;
import com.app.dto.MercadoPagoWebhookDTO;
import com.app.dto.ProductoCompraDTO;
import com.app.dto.ProductoDTO;
import com.app.dto.SalaDTO;
import com.app.dto.UsuarioDTO;
import com.app.dto.VentaAltaDTO;
import com.app.dto.VentaDTO;
import com.app.enums.EstadoReembolso;
import com.app.enums.EstadoVenta;
import com.app.exception.AccesoDenegadoException;
import com.app.exception.ButacaNoDisponibleException;
import com.app.exception.PagoRechazadoException;
import com.app.exception.RecursoNoEncontradoException;
import com.app.exception.ServicioNoDisponibleException;
import com.app.exception.SolicitudInvalidaException;
import com.app.mapper.IMapper;
import com.app.model.Venta;
import com.app.model.VentaButaca;
import com.app.model.VentaProducto;
import com.app.repository.VentaRepository;
import com.app.senders.EventoEntradaCompradaDTO;
import com.app.senders.FacturaItemDTO;
import com.app.senders.VentaCanceladaEvent;
import com.app.senders.VentaConfirmadaEvent;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

    private static final String BUTACA_DISPONIBLE = "DISPONIBLE";
    private static final String BUTACA_OCUPADO = "OCUPADO";

    private final VentaRepository ventaRepository;
    private final RabbitTemplate rabbitTemplate;
    private final SalaClient salaClient;
    private final ProductoClient productoClient;
    private final AuthClient authClient;
    private final MercadoPagoClient mercadoPagoClient;

    @Value("${mercadopago.notification-url:http://localhost:8081/ventas/webhook/mercadopago}")
    private String notificationUrl;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Override
    @Transactional
    public VentaDTO altaVenta(IMapper<Venta> mapper, String username) {
        Venta venta = mapper.mapperTo();
        venta.setUsername(username);
        venta.setFecha(LocalDateTime.now());

        VentaAltaDTO dto = dto(mapper);
        List<Long> butacaIds = dto != null ? dto.butacaIds() : List.of();
        if (butacaIds == null || butacaIds.isEmpty()) {
            throw new SolicitudInvalidaException("Hay que elegir al menos una butaca");
        }
        if (butacaIds.size() != butacaIds.stream().distinct().count()) {
            throw new SolicitudInvalidaException("No se puede elegir la misma butaca mas de una vez");
        }


        FuncionDTO funcion = obtenerFuncion(venta.getFuncionId());
        List<ButacaDTO> butacasSala = obtenerButacasDeSala(funcion.salaId());
        Map<Long, ButacaDTO> porId = butacasSala.stream()
                .collect(Collectors.toMap(ButacaDTO::id, b -> b));

        BigDecimal precioEntrada = funcion.precioBase() != null ? funcion.precioBase() : BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        for (Long butacaId : butacaIds) {
            ButacaDTO butaca = porId.get(butacaId);
            if (butaca == null) {
                throw new SolicitudInvalidaException(
                        "La butaca " + butacaId + " no existe en la sala de la funcion " + funcion.id());
            }
            if (!butaca.disponible()) {
                throw new ButacaNoDisponibleException("La butaca " + butaca.fila() + butaca.numero() + " no esta disponible");
            }
            venta.addButaca(new VentaButaca(butaca.id(), butaca.fila(), butaca.numero()));
            total = total.add(precioEntrada);
        }

        if (dto != null && dto.productos() != null) {
            for (ProductoCompraDTO linea : dto.productos()) {
                ProductoDTO producto = obtenerProducto(linea.productoId());

                if (producto.stock() == null || producto.stock() < linea.cantidad()) {
                    throw new ButacaNoDisponibleException("No hay stock suficiente de " + producto.nombre());
                }

                VentaProducto vp = new VentaProducto();
                vp.setProductoId(producto.id());
                vp.setCantidad(linea.cantidad());
                vp.setPrecioUnitario(producto.precio());
                venta.addProducto(vp);

                total = total.add(vp.subtotal());
            }
        }

        venta.setTotal(total);
        venta.setEstado(EstadoVenta.PENDIENTE);

        venta = ventaRepository.saveAndFlush(venta);

        String initPoint = null;
        try {
            MercadoPagoPreferenceResponse preferencia = crearPreferenciaPago(venta, precioEntrada);
            venta.setPreferenceId(preferencia.id());
            initPoint = preferencia.initPoint();
        } catch (PagoRechazadoException | ServicioNoDisponibleException e) {
            log.warn("No se pudo generar la preferencia de pago en Mercado Pago para la venta {}: {}. " +
                    "La venta queda creada en PENDIENTE sin link de pago real.", venta.getId(), e.getMessage());
        }

        return VentaDTO.from(venta, initPoint);
    }

    @Override
    public List<VentaDTO> listarVentas() {
        return ventaRepository.findAll().stream().map(VentaDTO::from).toList();
    }

    @Override
    public List<VentaDTO> listarVentasDeUsuario(String username) {
        return ventaRepository.findByUsername(username).stream().map(VentaDTO::from).toList();
    }

    @Override
    public VentaDTO obtenerPorId(Long id) {
        return VentaDTO.from(buscar(id));
    }

    @Override
    @Transactional
    public VentaDTO cancelarVenta(Long id, String username, String rolesCsv) {
        Venta venta = buscar(id);
        verificarPropietarioOAdmin(venta, username, rolesCsv);

        if (venta.getEstado() == EstadoVenta.CANCELADA) {
            throw new ButacaNoDisponibleException("La venta " + id + " ya estaba cancelada");
        }

        EstadoVenta estadoAnterior = venta.getEstado();
        venta.setEstado(EstadoVenta.CANCELADA);

        for (VentaButaca vb : venta.getButacas()) {
            publicarCambioButaca(vb.getButacaId(), BUTACA_DISPONIBLE);
        }

        if (estadoAnterior == EstadoVenta.PAGADA) {
            venta.setEstadoReembolso(intentarReembolso(venta));

            for (VentaProducto vp : venta.getProductos()) {
                ajustarStockProducto(vp.getProductoId(), vp.getCantidad());
            }
        } else {
            venta.setEstadoReembolso(EstadoReembolso.NO_APLICA);
        }

        venta = ventaRepository.save(venta);

        publicar(RabbitMQConfig.CANCELADA_ROUTING_KEY, armarEventoCancelacion(venta));

        return VentaDTO.from(venta);
    }

    private VentaCanceladaEvent armarEventoCancelacion(Venta venta) {
        VentaCanceladaEvent evento = new VentaCanceladaEvent();
        evento.setIdVenta(venta.getId());
        evento.setUsername(venta.getUsername());
        evento.setNumeroCompra("V-" + venta.getId());
        evento.setPeliculaTitulo(venta.getPeliculaTitulo());
        evento.setTotal(venta.getTotal());
        evento.setEstadoReembolso(venta.getEstadoReembolso().name());
        evento.setButacas(venta.getButacas().stream()
                .map(vb -> (vb.getFila() != null ? vb.getFila() : "") + (vb.getNumero() != null ? vb.getNumero() : ""))
                .toList());

        UsuarioDTO usuario = obtenerUsuarioOpcional(venta.getUsername());
        if (usuario != null) {
            evento.setNombreCompleto(usuario.nombreCompleto());
        }

        return evento;
    }

    @Override
    @Transactional
    public VentaDTO reintentarPago(Long id, String username, String rolesCsv) {
        Venta venta = buscar(id);
        verificarPropietarioOAdmin(venta, username, rolesCsv);

        if (venta.getEstado() != EstadoVenta.PENDIENTE) {
            throw new SolicitudInvalidaException(
                    "La venta " + id + " ya no esta pendiente de pago (estado actual: " + venta.getEstado() + ")");
        }

        BigDecimal subtotalProductos = venta.getProductos().stream()
                .map(VentaProducto::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int cantButacas = venta.getButacas().size();
        BigDecimal precioEntrada = cantButacas > 0
                ? venta.getTotal().subtract(subtotalProductos)
                        .divide(BigDecimal.valueOf(cantButacas), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        MercadoPagoPreferenceResponse preferencia = crearPreferenciaPago(venta, precioEntrada);
        venta.setPreferenceId(preferencia.id());
        venta = ventaRepository.save(venta);

        return VentaDTO.from(venta, preferencia.initPoint());
    }

    @Override
    @Transactional
    public void procesarWebhookMercadoPago(MercadoPagoWebhookDTO webhook) {
        if (webhook == null || webhook.data() == null || webhook.data().id() == null
                || !"payment".equalsIgnoreCase(webhook.type())) {
            return;
        }

        MercadoPagoPaymentResponse pago;
        try {
            pago = mercadoPagoClient.obtenerPago(webhook.data().id());
        } catch (FeignException e) {
            log.error("No se pudo consultar el pago {} en Mercado Pago: {}", webhook.data().id(), e.getMessage());
            return;
        }

        if (!pago.aprobado()) {
            log.info("Webhook de pago {} con estado {}: no se confirma la venta todavia", pago.id(), pago.status());
            return;
        }

        Long ventaId;
        try {
            ventaId = Long.valueOf(pago.externalReference());
        } catch (NumberFormatException e) {
            log.error("external_reference invalido en el pago {}: {}", pago.id(), pago.externalReference());
            return;
        }

        Venta venta = ventaRepository.findById(ventaId).orElse(null);
        if (venta == null) {
            log.error("Webhook de pago {} referencia la venta {}, que no existe", pago.id(), ventaId);
            return;
        }

        if (venta.getEstado() == EstadoVenta.PAGADA) {
            return;
        }
        if (venta.getEstado() == EstadoVenta.CANCELADA) {
            log.warn("Llego confirmacion de pago para la venta {} pero ya estaba CANCELADA", ventaId);
            return;
        }

        venta.setEstado(EstadoVenta.PAGADA);
        venta.setPaymentId(String.valueOf(pago.id()));

        for (VentaButaca vb : venta.getButacas()) {
            publicarCambioButaca(vb.getButacaId(), BUTACA_OCUPADO);
        }

        for (VentaProducto vp : venta.getProductos()) {
            ajustarStockProducto(vp.getProductoId(), -vp.getCantidad());
        }

        ventaRepository.save(venta);

        publicar(RabbitMQConfig.CONFIRMADA_ROUTING_KEY, armarFactura(venta));
    }

    private VentaConfirmadaEvent armarFactura(Venta venta) {
        VentaConfirmadaEvent evento = new VentaConfirmadaEvent();
        evento.setIdVenta(venta.getId());
        evento.setUsername(venta.getUsername());
        evento.setFuncionId(venta.getFuncionId());
        evento.setPeliculaTitulo(venta.getPeliculaTitulo());
        evento.setNumeroCompra("V-" + venta.getId());
        evento.setFecha(venta.getFecha());
        evento.setTotal(venta.getTotal());
        evento.setButacas(venta.getButacas().stream()
                .map(vb -> (vb.getFila() != null ? vb.getFila() : "") + (vb.getNumero() != null ? vb.getNumero() : ""))
                .toList());

        UsuarioDTO usuario = obtenerUsuarioOpcional(venta.getUsername());
        if (usuario != null) {
            evento.setNombreCompleto(usuario.nombreCompleto());
        }

        FuncionDTO funcion = obtenerFuncionOpcional(venta.getFuncionId());
        BigDecimal precioEntrada = BigDecimal.ZERO;
        if (funcion != null) {
            evento.setFuncionFechaHora(parseFechaHora(funcion.fechaHora()));
            precioEntrada = funcion.precioBase() != null ? funcion.precioBase() : BigDecimal.ZERO;

            SalaDTO sala = obtenerSalaOpcional(funcion.salaId());
            if (sala != null) {
                evento.setSalaNombre(sala.nombre());
                evento.setSalaTipo(sala.tipo());
            } else if (funcion.nombreSala() != null) {
                evento.setSalaNombre(funcion.nombreSala());
            }
        }

        int cantButacas = venta.getButacas().size();
        BigDecimal subtotalEntradas = precioEntrada.multiply(BigDecimal.valueOf(cantButacas));
        String etiquetaButacas = evento.getButacas().isEmpty() ? "Entrada" : String.join(", ", evento.getButacas());
        evento.setEntradas(List.of(new FacturaItemDTO(
                "Entrada (butacas " + etiquetaButacas + ")", cantButacas, precioEntrada, subtotalEntradas)));
        evento.setSubtotalEntradas(subtotalEntradas);

        List<FacturaItemDTO> items = new ArrayList<>();
        BigDecimal subtotalProductos = BigDecimal.ZERO;
        for (VentaProducto vp : venta.getProductos()) {
            String nombre = obtenerNombreProductoOpcional(vp.getProductoId());
            items.add(new FacturaItemDTO(nombre, vp.getCantidad(), vp.getPrecioUnitario(), vp.subtotal()));
            subtotalProductos = subtotalProductos.add(vp.subtotal());
        }
        evento.setProductos(items);
        evento.setSubtotalProductos(subtotalProductos);

        return evento;
    }

    private LocalDateTime parseFechaHora(String fechaHora) {
        if (fechaHora == null) return null;
        try {
            return LocalDateTime.parse(fechaHora);
        } catch (Exception e) {
            return null;
        }
    }

    private FuncionDTO obtenerFuncionOpcional(Long funcionId) {
        try {
            return salaClient.obtenerFuncion(funcionId);
        } catch (FeignException e) {
            log.warn("No se pudo completar la funcion {} para la factura (no bloquea la venta): {}", funcionId, e.getMessage());
            return null;
        }
    }

    private SalaDTO obtenerSalaOpcional(Long salaId) {
        if (salaId == null) return null;
        try {
            return salaClient.obtenerSala(salaId);
        } catch (FeignException e) {
            log.warn("No se pudo completar la sala {} para la factura (no bloquea la venta): {}", salaId, e.getMessage());
            return null;
        }
    }

    private UsuarioDTO obtenerUsuarioOpcional(String username) {
        try {
            return authClient.obtenerUsuario(username);
        } catch (FeignException e) {
            log.warn("No se pudo resolver nombre/apellido de {} para la factura (no bloquea la venta): {}", username, e.getMessage());
            return null;
        }
    }

    private String obtenerNombreProductoOpcional(Long productoId) {
        try {
            return productoClient.obtenerProducto(productoId).nombre();
        } catch (FeignException e) {
            log.warn("No se pudo completar el nombre del producto {} para la factura (no bloquea la venta): {}", productoId, e.getMessage());
            return "Producto #" + productoId;
        }
    }


    private Venta buscar(Long id) {
        return ventaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la venta con id " + id));
    }

    private void verificarPropietarioOAdmin(Venta venta, String username, String rolesCsv) {
        if (username != null && username.equals(venta.getUsername())) {
            return;
        }
        Set<String> roles = rolesCsv == null || rolesCsv.isBlank()
                ? Set.of()
                : Set.of(Arrays.stream(rolesCsv.split(",")).map(String::trim).toArray(String[]::new));
        if (roles.contains("ROLE_ADMIN") || roles.contains("ROLE_EMPLEADO")) {
            return;
        }
        throw new AccesoDenegadoException("No tenes permiso para operar sobre la venta " + venta.getId());
    }

    private FuncionDTO obtenerFuncion(Long funcionId) {
        try {
            return salaClient.obtenerFuncion(funcionId);
        } catch (FeignException.NotFound e) {
            throw new SolicitudInvalidaException("La funcion " + funcionId + " no existe");
        } catch (FeignException e) {
            throw new ServicioNoDisponibleException(
                    "No se pudo verificar la funcion " + funcionId + " (el microservicio Sala no responde). Intenta de nuevo.");
        }
    }

    private List<ButacaDTO> obtenerButacasDeSala(Long salaId) {
        try {
            return salaClient.obtenerButacasPorSala(salaId);
        } catch (FeignException.NotFound e) {
            throw new SolicitudInvalidaException("La sala " + salaId + " no existe");
        } catch (FeignException e) {
            throw new ServicioNoDisponibleException(
                    "No se pudo verificar las butacas de la sala " + salaId + " (el microservicio Sala no responde). Intenta de nuevo.");
        }
    }

    private ProductoDTO obtenerProducto(Long productoId) {
        try {
            return productoClient.obtenerProducto(productoId);
        } catch (FeignException.NotFound e) {
            throw new SolicitudInvalidaException("El producto " + productoId + " no existe");
        } catch (FeignException e) {
            throw new ServicioNoDisponibleException(
                    "No se pudo verificar el producto " + productoId + " (el microservicio Productos no responde). Intenta de nuevo.");
        }
    }

    private MercadoPagoPreferenceResponse crearPreferenciaPago(Venta venta, BigDecimal precioEntrada) {
        List<MercadoPagoPreferenceRequest.Item> items = new ArrayList<>();
        for (VentaButaca vb : venta.getButacas()) {
            items.add(new MercadoPagoPreferenceRequest.Item(
                    "Entrada - butaca " + vb.getFila() + vb.getNumero() + " (funcion " + venta.getFuncionId() + ")",
                    1, precioEntrada));
        }
        for (VentaProducto vp : venta.getProductos()) {
            items.add(new MercadoPagoPreferenceRequest.Item(
                    "Producto #" + vp.getProductoId(), vp.getCantidad(), vp.getPrecioUnitario()));
        }

        MercadoPagoPreferenceRequest.BackUrls backUrls = new MercadoPagoPreferenceRequest.BackUrls(
                frontendUrl + "/historial?pago=exitoso&venta=" + venta.getId(),
                frontendUrl + "/historial?pago=fallido&venta=" + venta.getId(),
                frontendUrl + "/historial?pago=pendiente&venta=" + venta.getId());

        MercadoPagoPreferenceRequest request = new MercadoPagoPreferenceRequest(
                items, String.valueOf(venta.getId()), notificationUrl, backUrls, null);

        try {
            return mercadoPagoClient.crearPreferencia(request);
        } catch (FeignException.FeignClientException e) {
            throw new PagoRechazadoException("Mercado Pago rechazo la solicitud de pago: " + e.getMessage());
        } catch (FeignException e) {
            throw new ServicioNoDisponibleException(
                    "No se pudo generar la preferencia de pago en Mercado Pago. Intenta de nuevo.");
        }
    }

    private EstadoReembolso intentarReembolso(Venta venta) {
        if (venta.getPaymentId() == null) {
            log.warn("La venta {} estaba PAGADA pero no tiene paymentId: no se puede reembolsar", venta.getId());
            return EstadoReembolso.FALLIDO;
        }
        try {
            MercadoPagoRefundResponse respuesta = mercadoPagoClient.reembolsar(venta.getPaymentId());
            return "approved".equalsIgnoreCase(respuesta.status()) ? EstadoReembolso.COMPLETADO : EstadoReembolso.FALLIDO;
        } catch (FeignException e) {
            log.error("No se pudo reembolsar el pago {} de la venta {} en Mercado Pago: {}",
                    venta.getPaymentId(), venta.getId(), e.getMessage());
            return EstadoReembolso.FALLIDO;
        }
    }

    private static final int INTENTOS_AJUSTE_STOCK = 3;

    private void ajustarStockProducto(Long productoId, int delta) {
        for (int intento = 1; intento <= INTENTOS_AJUSTE_STOCK; intento++) {
            try {
                productoClient.ajustarStock(productoId, new AjustarStockDTO(delta));
                return;
            } catch (FeignException.Conflict e) {
                if (intento == INTENTOS_AJUSTE_STOCK) {
                    log.warn("No se pudo ajustar el stock del producto {} en {} tras {} intentos " +
                            "(conflicto de version persistente): {}", productoId, delta, intento, e.getMessage());
                    return;
                }
                log.info("Conflicto de version ajustando stock del producto {} en {} (intento {}/{}), reintentando",
                        productoId, delta, intento, INTENTOS_AJUSTE_STOCK);
            } catch (FeignException e) {
                log.warn("No se pudo ajustar el stock del producto {} en {} (Productos caido o rechazo el ajuste): {}",
                        productoId, delta, e.getMessage());
                return;
            } catch (Exception e) {
                log.warn("Error inesperado ajustando stock del producto {} en {}: {}", productoId, delta, e.getMessage());
                return;
            }
        }
    }

    private void publicarCambioButaca(Long butacaId, String nuevoEstado) {
        publicar(RabbitMQConfig.ROUTING_KEY_BUTACAS, RabbitMQConfig.EXCHANGE_VENTAS_SALA,
                new EventoEntradaCompradaDTO(butacaId, nuevoEstado));
    }

    private void publicar(String routingKey, Object evento) {
        publicar(routingKey, RabbitMQConfig.EXCHANGE, evento);
    }

    private void publicar(String routingKey, String exchange, Object evento) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    enviar(exchange, routingKey, evento);
                }
            });
        } else {
            enviar(exchange, routingKey, evento);
        }
    }

    private void enviar(String exchange, String routingKey, Object evento) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, evento);
        } catch (AmqpException e) {
            log.warn("No se pudo publicar el evento {} ({} / {}) en RabbitMQ (¿esta levantado el broker?). " +
                    "El cambio igual se guardo. Detalle: {}", evento, exchange, routingKey, e.getMessage());
        }
    }

    private VentaAltaDTO dto(IMapper<Venta> mapper) {
        return mapper instanceof VentaAltaDTO v ? v : null;
    }
}
