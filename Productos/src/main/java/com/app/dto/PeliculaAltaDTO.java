package com.app.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PeliculaAltaDTO(
    @NotBlank(message = "El titulo es obligatorio")
    @Size(max = 150, message = "El titulo no puede superar los 150 caracteres")
    String titulo,

    @Size(max = 100, message = "El genero no puede superar los 100 caracteres")
    String genero,

    @NotNull(message = "La duracion es obligatoria")
    @Positive(message = "La duracion debe ser mayor a 0")
    Integer duracion,

    @Size(max = 100, message = "El director no puede superar los 100 caracteres")
    String director,

    @Size(max = 100, message = "El guionista no puede superar los 100 caracteres")
    String guionista,

    LocalDate fechaEstreno,

    @Size(max = 1000, message = "La sinopsis no puede superar los 1000 caracteres")
    String sinopsis,

    @Size(max = 20, message = "La clasificacion de edad no puede superar los 20 caracteres")
    String clasificacionEdad,

    @DecimalMin(value = "0.0", message = "La calificacion no puede ser negativa")
    @DecimalMax(value = "5.0", message = "La calificacion no puede superar 5.0")
    Double calificacion,

    @Size(max = 500, message = "La URL del poster no puede superar los 500 caracteres")
    String poster,

    @Size(max = 255, message = "El reparto no puede superar los 255 caracteres")
    String reparto,

    @Size(max = 100, message = "El formato disponible no puede superar los 100 caracteres")
    String formatoDisponible
) {}
