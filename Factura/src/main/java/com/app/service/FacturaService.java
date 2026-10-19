package com.app.service;

import com.app.dto.FacturaDTO;
import com.app.dto.VentaConfirmadaEventDTO;

import java.util.List;

public interface FacturaService {

    void procesarVentaConfirmada(VentaConfirmadaEventDTO evento);

    List<FacturaDTO> listarDeUsuario(String username);

    FacturaDTO obtenerPorId(Long id);

    FacturaDTO obtenerPorVentaId(Long ventaId);
}
