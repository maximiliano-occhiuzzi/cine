package com.app.senders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaConfirmadaEvent {

    private Long idVenta;
    private String username;

    private String nombreCompleto;

    private Long funcionId;

    private String numeroCompra;

    private LocalDateTime fecha;

    private String peliculaTitulo;

    private String salaNombre;
    private String salaTipo;

    private LocalDateTime funcionFechaHora;

    private List<String> butacas;

    private List<FacturaItemDTO> entradas;
    private List<FacturaItemDTO> productos;

    private BigDecimal subtotalEntradas;
    private BigDecimal subtotalProductos;
    private BigDecimal total;
}
