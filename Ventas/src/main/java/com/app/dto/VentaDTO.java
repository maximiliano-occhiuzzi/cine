package com.app.dto;

import com.app.enums.EstadoReembolso;
import com.app.enums.EstadoVenta;
import com.app.model.Venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VentaDTO(
    Long id,
    String username,
    Long funcionId,
    String peliculaTitulo,
    List<VentaButacaDTO> butacas,
    EstadoVenta estado,
    EstadoReembolso estadoReembolso,
    BigDecimal total,
    LocalDateTime fecha,
    List<VentaProductoDTO> productos,
    String initPoint,
    boolean tienePreferenciaDePago
) {
    public static VentaDTO from(Venta venta) {
        return from(venta, null);
    }

    public static VentaDTO from(Venta venta, String initPoint) {
        return new VentaDTO(
                venta.getId(),
                venta.getUsername(),
                venta.getFuncionId(),
                venta.getPeliculaTitulo(),
                venta.getButacas().stream().map(VentaButacaDTO::from).toList(),
                venta.getEstado(),
                venta.getEstadoReembolso(),
                venta.getTotal(),
                venta.getFecha(),
                venta.getProductos().stream().map(VentaProductoDTO::from).toList(),
                initPoint,
                venta.getPreferenceId() != null
        );
    }
}
