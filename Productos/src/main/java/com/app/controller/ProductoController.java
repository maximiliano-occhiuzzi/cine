package com.app.controller;

import com.app.dto.ProductoAltaDTO;
import com.app.dto.ProductoModificarDTO;
import com.app.dto.ProductoSalidaDTO;
import com.app.dto.ProductoStockDTO;
import com.app.enums.CategoriaProducto;
import com.app.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @PostMapping
    public ResponseEntity<ProductoSalidaDTO> altaProducto(@Valid @RequestBody ProductoAltaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.altaProducto(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoSalidaDTO> modificarProducto(@PathVariable Long id,
                                                               @Valid @RequestBody ProductoModificarDTO dto) {
        return ResponseEntity.ok(productoService.modificarProducto(id, dto));
    }

    @GetMapping
    public ResponseEntity<Page<ProductoSalidaDTO>> listarProductos(
            @RequestParam(required = false) CategoriaProducto categoria,
            @RequestParam(required = false) String nombre,
            @RequestParam(defaultValue = "false") boolean incluirInactivos,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(productoService.listarProductos(categoria, nombre, incluirInactivos, pageable));
    }

    @GetMapping("/bajo-stock")
    public ResponseEntity<List<ProductoSalidaDTO>> listarBajoStock(
            @RequestParam(defaultValue = "5") int umbral) {
        return ResponseEntity.ok(productoService.listarBajoStock(umbral));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoSalidaDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductoSalidaDTO> ajustarStock(@PathVariable Long id,
                                                          @Valid @RequestBody ProductoStockDTO dto) {
        return ResponseEntity.ok(productoService.ajustarStock(id, dto.cantidad()));
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<ProductoSalidaDTO> reactivarProducto(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.reactivarProducto(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }
}
