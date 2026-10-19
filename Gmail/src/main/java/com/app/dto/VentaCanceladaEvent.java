package com.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public record VentaCanceladaEvent(
    Long idVenta,
    String username,
    String nombreCompleto,
    String numeroCompra,
    String peliculaTitulo,
    List<String> butacas,
    BigDecimal total,
    String estadoReembolso
) {}
