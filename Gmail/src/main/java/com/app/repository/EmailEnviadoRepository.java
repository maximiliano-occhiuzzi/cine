package com.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.model.EmailEnviado;

public interface EmailEnviadoRepository extends JpaRepository<EmailEnviado, Long> {

    boolean existsByVentaIdAndTipo(Long ventaId, String tipo);
}
