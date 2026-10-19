package com.app.dto;

import com.app.enums.TipoSala;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record SalaEditarDTO(
    @NotBlank(message = "El nombre de la sala es obligatorio")
    String nombre,

    @NotNull(message = "El tipo de sala es obligatorio")
    TipoSala tipo
) {}
