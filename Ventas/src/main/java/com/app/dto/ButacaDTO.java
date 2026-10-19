package com.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public record ButacaDTO(
    Long id,
    Long salaId,
    String fila,
    Integer numero,
    String estado
) {
    public boolean disponible() {
        return "DISPONIBLE".equalsIgnoreCase(estado);
    }
}
