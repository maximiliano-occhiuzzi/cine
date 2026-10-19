package com.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "emails_enviados",
        uniqueConstraints = @UniqueConstraint(columnNames = {"venta_id", "tipo"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailEnviado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "venta_id")
    private Long ventaId;

    private String tipo;

    private String destinatario;
    private String asunto;
    private LocalDateTime fecha;

    private String estado;

    @Column(length = 1000)
    private String detalleError;
}
