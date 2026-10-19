package com.app.service;

import com.app.client.AuthClient;
import com.app.dto.FacturaDTO;
import com.app.dto.FacturaItemEventDTO;
import com.app.dto.UsuarioDTO;
import com.app.dto.VentaConfirmadaEventDTO;
import com.app.exception.RecursoNoEncontradoException;
import com.app.model.Factura;
import com.app.model.FacturaItem;
import com.app.repository.FacturaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacturaServiceImpl implements FacturaService {

    private final FacturaRepository facturaRepository;
    private final AuthClient authClient;

    @Override
    @Transactional
    public void procesarVentaConfirmada(VentaConfirmadaEventDTO evento) {
        if (facturaRepository.findByVentaId(evento.idVenta()).isPresent()) {
            log.info("Ya existe una factura para la venta {}, se ignora el mensaje duplicado", evento.idVenta());
            return;
        }

        Factura factura = new Factura();
        factura.setVentaId(evento.idVenta());
        factura.setNumeroCompra(evento.numeroCompra());
        factura.setUsername(evento.username());
        factura.setNombreCompleto(evento.nombreCompleto());
        factura.setFuncionId(evento.funcionId());
        factura.setPeliculaTitulo(evento.peliculaTitulo());
        factura.setSalaNombre(evento.salaNombre());
        factura.setSalaTipo(evento.salaTipo());
        factura.setFuncionFechaHora(evento.funcionFechaHora());
        factura.setFecha(evento.fecha());
        factura.setButacas(evento.butacas() != null ? evento.butacas() : List.of());
        factura.setEntradas(aItems(evento.entradas()));
        factura.setProductos(aItems(evento.productos()));
        factura.setSubtotalEntradas(evento.subtotalEntradas() != null ? evento.subtotalEntradas() : BigDecimal.ZERO);
        factura.setSubtotalProductos(evento.subtotalProductos() != null ? evento.subtotalProductos() : BigDecimal.ZERO);
        factura.setTotal(evento.total() != null ? evento.total() : BigDecimal.ZERO);

        if (factura.getNombreCompleto() == null || factura.getNombreCompleto().isBlank()) {
            UsuarioDTO usuario = obtenerUsuarioOpcional(evento.username());
            if (usuario != null) {
                String nombreCompleto = ((usuario.nombre() != null ? usuario.nombre() : "") + " " +
                        (usuario.apellido() != null ? usuario.apellido() : "")).trim();
                factura.setNombreCompleto(nombreCompleto);
            }
        }

        facturaRepository.save(factura);
        log.info("Factura {} persistida para la venta {}", factura.getNumeroCompra(), evento.idVenta());
    }

    @Override
    public List<FacturaDTO> listarDeUsuario(String username) {
        return facturaRepository.findByUsername(username).stream().map(FacturaDTO::from).toList();
    }

    @Override
    public FacturaDTO obtenerPorId(Long id) {
        return facturaRepository.findById(id)
                .map(FacturaDTO::from)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la factura " + id));
    }

    @Override
    public FacturaDTO obtenerPorVentaId(Long ventaId) {
        return facturaRepository.findByVentaId(ventaId)
                .map(FacturaDTO::from)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe factura para la venta " + ventaId));
    }

    private List<FacturaItem> aItems(List<FacturaItemEventDTO> items) {
        if (items == null) return List.of();
        return items.stream()
                .map(i -> new FacturaItem(i.nombre(), i.cantidad(), i.precioUnitario(), i.subtotal()))
                .toList();
    }

    private UsuarioDTO obtenerUsuarioOpcional(String username) {
        try {
            return authClient.obtenerUsuario(username);
        } catch (Exception e) {
            log.warn("No se pudo resolver el usuario {} para la factura (no bloquea la persistencia): {}", username, e.getMessage());
            return null;
        }
    }
}
