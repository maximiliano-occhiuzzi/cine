package com.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public record VentaConfirmadaEvent(
    Long idVenta,
    String username,
    String nombreCompleto,
    Long funcionId,
    String numeroCompra,
    LocalDateTime fecha,
    String peliculaTitulo,
    String salaNombre,
    String salaTipo,
    LocalDateTime funcionFechaHora,
    List<String> butacas,
    List<FacturaItemEventDTO> entradas,
    List<FacturaItemEventDTO> productos,
    BigDecimal subtotalEntradas,
    BigDecimal subtotalProductos,
    BigDecimal total
) {}
