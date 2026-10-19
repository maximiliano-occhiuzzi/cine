package com.app.dto;

import com.app.enums.CategoriaProducto;
import com.app.model.Producto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductoSalidaDTO(
    Long id,
    String nombre,
    String descripcion,
    BigDecimal precio,
    Integer stock,
    CategoriaProducto categoria,
    String imagenUrl,
    Boolean activo,
    LocalDateTime creadoEn,
    LocalDateTime actualizadoEn
) {

    public static ProductoSalidaDTO from(Producto producto) {
        return new ProductoSalidaDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getStock(),
                producto.getCategoria(),
                producto.getImagenUrl(),
                producto.getActivo(),
                producto.getCreadoEn(),
                producto.getActualizadoEn()
        );
    }
}
