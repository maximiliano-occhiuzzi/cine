package com.app.dto;

import java.math.BigDecimal;

/**
 * Evento que MS Ventas publica en la cola {@code venta.eventos} cuando se
 * confirma una compra. El JSON que viaja por RabbitMQ tiene exactamente estos
 * campos.
 *
 * <p>El converter de Jackson ignora las propiedades desconocidas, asi que si
 * Ventas agrega campos de mas no se rompe el consumo.</p>
 */
public record VentaConfirmadaEvent(
        Long ventaId,
        String destinatario,
        String pelicula,
        Integer cantidad,
        BigDecimal montoTotal
) {
}
