package com.app.senders;

import java.math.BigDecimal;


public record FacturaItemDTO(
    String nombre,
    Integer cantidad,
    BigDecimal precioUnitario,
    BigDecimal subtotal
) {}
