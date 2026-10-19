package com.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;


@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductoDTO(
    Long id,
    String nombre,
    BigDecimal precio,
    Integer stock
) {}
