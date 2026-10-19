package com.app.service;

import com.app.dto.MercadoPagoWebhookDTO;
import com.app.dto.VentaDTO;
import com.app.mapper.IMapper;
import com.app.model.Venta;

import java.util.List;

public interface VentaService {

    VentaDTO altaVenta(IMapper<Venta> mapper, String username);

    List<VentaDTO> listarVentas();

    List<VentaDTO> listarVentasDeUsuario(String username);

    VentaDTO obtenerPorId(Long id);

    VentaDTO cancelarVenta(Long id, String username, String rolesCsv);

    VentaDTO reintentarPago(Long id, String username, String rolesCsv);

    void procesarWebhookMercadoPago(MercadoPagoWebhookDTO webhook);
}
