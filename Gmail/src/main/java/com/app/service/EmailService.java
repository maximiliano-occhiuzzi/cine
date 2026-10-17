package com.app.service;

import java.util.List;

import com.app.dto.EnviarEmailDTO;
import com.app.model.EmailEnviado;

public interface EmailService {

    /** Envia un mail disparado a mano (sin venta asociada). */
    EmailEnviado enviar(EnviarEmailDTO dto);

    /**
     * Envia un mail y lo asocia a una venta. {@code ventaId} puede ser null
     * cuando el origen no es una venta.
     */
    EmailEnviado enviar(EnviarEmailDTO dto, Long ventaId);

    List<EmailEnviado> historial();
}
