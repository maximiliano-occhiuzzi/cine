package com.app.dto;

import jakarta.validation.constraints.NotNull;

public record ProductoStockDTO(
    @NotNull Integer cantidad
) {}
