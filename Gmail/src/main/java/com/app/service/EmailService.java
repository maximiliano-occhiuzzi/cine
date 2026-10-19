package com.app.service;

import java.util.List;

import com.app.dto.EnviarEmailDTO;
import com.app.model.EmailEnviado;

public interface EmailService {

    
    EmailEnviado enviar(EnviarEmailDTO dto);

    
    EmailEnviado enviar(EnviarEmailDTO dto, Long ventaId);

    
    EmailEnviado enviar(EnviarEmailDTO dto, Long ventaId, String tipo);

    List<EmailEnviado> historial();
}
