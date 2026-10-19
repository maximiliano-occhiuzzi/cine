package com.app.senders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaCanceladaEvent {

    private Long idVenta;
    private String username;

    private String nombreCompleto;

    private String numeroCompra;
    private String peliculaTitulo;
    private List<String> butacas;
    private BigDecimal total;

    private String estadoReembolso;
}
