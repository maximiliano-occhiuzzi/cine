package com.app.controller;

import com.app.dto.MercadoPagoWebhookDTO;
import com.app.dto.VentaAltaDTO;
import com.app.dto.VentaDTO;
import com.app.service.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ventas")
@Tag(name = "Ventas", description = "Compra, cancelacion y webhook de pago de entradas")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @PostMapping
    @Operation(summary = "Comprar una entrada (y opcionalmente productos de confiteria)")
    public ResponseEntity<VentaDTO> altaVenta(@RequestHeader("X-User-Username") String username,
                                               @Valid @RequestBody VentaAltaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ventaService.altaVenta(dto, username));
    }

    @GetMapping
    @Operation(summary = "Listar todas las ventas (ADMIN/EMPLEADO)")
    public ResponseEntity<List<VentaDTO>> listarVentas() {
        return ResponseEntity.ok(ventaService.listarVentas());
    }

    @GetMapping("/mias")
    @Operation(summary = "Listar las compras del usuario logueado")
    public ResponseEntity<List<VentaDTO>> misVentas(@RequestHeader("X-User-Username") String username) {
        return ResponseEntity.ok(ventaService.listarVentasDeUsuario(username));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una venta por id (ADMIN/EMPLEADO)")
    public ResponseEntity<VentaDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ventaService.obtenerPorId(id));
    }

    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar una venta: libera la butaca y, si ya estaba pagada, intenta el reembolso")
    public ResponseEntity<VentaDTO> cancelarVenta(@PathVariable Long id,
                                                   @RequestHeader("X-User-Username") String username,
                                                   @RequestHeader(value = "X-User-Roles", required = false, defaultValue = "") String roles) {
        return ResponseEntity.ok(ventaService.cancelarVenta(id, username, roles));
    }

    @PostMapping("/{id}/reintentar-pago")
    @Operation(summary = "Volver a generar el link de pago de una venta propia que quedo pendiente")
    public ResponseEntity<VentaDTO> reintentarPago(@PathVariable Long id,
                                                    @RequestHeader("X-User-Username") String username,
                                                    @RequestHeader(value = "X-User-Roles", required = false, defaultValue = "") String roles) {
        return ResponseEntity.ok(ventaService.reintentarPago(id, username, roles));
    }

    @PostMapping("/webhook/mercadopago")
    @Operation(summary = "Webhook de Mercado Pago (llamado por Mercado Pago, sin auth nuestra)")
    public ResponseEntity<Void> webhookMercadoPago(@RequestBody MercadoPagoWebhookDTO webhook) {
        ventaService.procesarWebhookMercadoPago(webhook);
        return ResponseEntity.ok().build();
    }
}
