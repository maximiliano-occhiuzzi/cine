package com.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public record UsuarioDTO(String username, String nombre, String apellido) {

    public String nombreCompleto() {
        if (nombre == null && apellido == null) return null;
        return ((nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "")).trim();
    }
}
