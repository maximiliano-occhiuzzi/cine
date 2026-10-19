package com.app.model;

import com.app.enums.EstadoReembolso;
import com.app.enums.EstadoVenta;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "venta")
@Data
@NoArgsConstructor
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private Long funcionId;

    private String peliculaTitulo;

    @Enumerated(EnumType.STRING)
    private EstadoVenta estado = EstadoVenta.PENDIENTE;

    @Enumerated(EnumType.STRING)
    private EstadoReembolso estadoReembolso = EstadoReembolso.NO_APLICA;

    private BigDecimal total = BigDecimal.ZERO;
    private LocalDateTime fecha;

    private String preferenceId;
    private String paymentId;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<VentaButaca> butacas = new ArrayList<>();

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<VentaProducto> productos = new ArrayList<>();

    public void addButaca(VentaButaca vb) {
        vb.setVenta(this);
        butacas.add(vb);
    }

    public void addProducto(VentaProducto vp) {
        vp.setVenta(this);
        productos.add(vp);
    }
}
