package com.app.dto;

import java.math.BigDecimal;

public record FacturaItemEventDTO(String nombre, Integer cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {}
