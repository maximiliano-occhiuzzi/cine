package com.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.model.EmailEnviado;

public interface EmailEnviadoRepository extends JpaRepository<EmailEnviado, Long> {

    /** Para no volver a mandar el mail de una venta que ya se proceso. */
    boolean existsByVentaId(Long ventaId);
}
