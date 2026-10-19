package com.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public record UsuarioDTO(String username, String nombre, String apellido, String email) {}
