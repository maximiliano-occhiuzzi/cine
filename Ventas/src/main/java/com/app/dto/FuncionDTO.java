package com.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;


@JsonIgnoreProperties(ignoreUnknown = true)
public record FuncionDTO(
    Long id,
    Long salaId,
    String nombreSala,
    Long peliculaId,
    String fechaHora,
    BigDecimal precioBase
) {}
