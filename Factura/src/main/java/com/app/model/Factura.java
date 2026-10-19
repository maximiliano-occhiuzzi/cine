package com.app.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "factura")
@Data
@NoArgsConstructor
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Long ventaId;
    private String numeroCompra;
    private String username;
    private String nombreCompleto;
    private Long funcionId;
    private String peliculaTitulo;
    private String salaNombre;
    private String salaTipo;
    private LocalDateTime funcionFechaHora;
    private LocalDateTime fecha;

    @ElementCollection
    @CollectionTable(name = "factura_butaca", joinColumns = @JoinColumn(name = "factura_id"))
    @Column(name = "butaca")
    private List<String> butacas = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "factura_item", joinColumns = @JoinColumn(name = "factura_id"))
    private List<FacturaItem> entradas = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "factura_item_producto", joinColumns = @JoinColumn(name = "factura_id"))
    private List<FacturaItem> productos = new ArrayList<>();

    private BigDecimal subtotalEntradas = BigDecimal.ZERO;
    private BigDecimal subtotalProductos = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;

}
