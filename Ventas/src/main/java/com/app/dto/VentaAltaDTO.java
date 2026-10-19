package com.app.dto;

import com.app.mapper.IMapper;
import com.app.model.Venta;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;


public record VentaAltaDTO(
    @NotNull Long funcionId,
    @NotEmpty List<Long> butacaIds,
    @Valid List<ProductoCompraDTO> productos,
    String peliculaTitulo
) implements IMapper<Venta> {

    @Override
    public Venta mapperTo() {
        Venta venta = new Venta();
        venta.setFuncionId(funcionId);
        venta.setPeliculaTitulo(peliculaTitulo);
        return venta;
    }
}
