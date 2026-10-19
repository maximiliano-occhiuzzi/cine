package com.app.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


public record ProductoCompraDTO(
    @NotNull Long productoId,
    @NotNull @Positive Integer cantidad
) {}
