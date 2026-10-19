package com.app.client;

import com.app.config.FeignConfig;
import com.app.dto.AjustarStockDTO;
import com.app.dto.ProductoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "modulo-productos", configuration = FeignConfig.class)
public interface ProductoClient {

    @GetMapping("/productos/{id}")
    ProductoDTO obtenerProducto(@PathVariable("id") Long id);

    @PatchMapping("/productos/{id}/stock")
    ProductoDTO ajustarStock(@PathVariable("id") Long id, @RequestBody AjustarStockDTO dto);
}
