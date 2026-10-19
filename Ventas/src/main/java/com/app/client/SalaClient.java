package com.app.client;

import com.app.config.FeignConfig;
import com.app.dto.ButacaDTO;
import com.app.dto.FuncionDTO;
import com.app.dto.SalaDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;


@FeignClient(name = "modulo-salas", configuration = FeignConfig.class)
public interface SalaClient {

    @GetMapping("/funciones/{id}")
    FuncionDTO obtenerFuncion(@PathVariable("id") Long id);

    @GetMapping("/salas/{id}")
    SalaDTO obtenerSala(@PathVariable("id") Long id);

    @GetMapping("/butacas/sala/{salaId}")
    List<ButacaDTO> obtenerButacasPorSala(@PathVariable("salaId") Long salaId);
}
