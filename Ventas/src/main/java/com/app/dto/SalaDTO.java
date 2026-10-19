package com.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public record SalaDTO(
    Long id,
    String nombre,
    Integer capacidad,
    String tipo
) {}
