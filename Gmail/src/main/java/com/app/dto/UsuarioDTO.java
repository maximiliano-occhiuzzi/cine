package com.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Copia del contrato real de Auth (solo lo que Gmail necesita). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UsuarioDTO(String username, String nombre, String apellido, String email) {}
