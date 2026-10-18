package com.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Copia EXACTA (lado CONSUMIDOR) del contrato que publica MS Ventas:
 * com.app.senders.VentaConfirmadaEvent. Mismos nombres de campo porque
 * viaja como JSON — si Ventas cambia esta clase, esta copia tiene que
 * actualizarse igual (ver CONTRATOS-REQUERIDOS.md de Ventas). No trae el
 * mail del comprador (solo el username): Gmail lo resuelve pegandole a
 * Auth (ver AuthClient).
 */
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
