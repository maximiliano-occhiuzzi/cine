package com.app.config;

import com.app.dto.FuncionAltaDTO;
import com.app.dto.SalaAltaDTO;
import com.app.enums.TipoSala;
import com.app.model.Sala;
import com.app.repository.SalaRepository;
import com.app.service.FuncionService;
import com.app.service.SalaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SalaRepository salaRepository;
    private final SalaService salaService;
    private final FuncionService funcionService;

    @Override
    public void run(String... args) {
        if (salaRepository.count() > 0) {
            return;
        }

        Sala sala1 = salaService.altaSalaConMapa(new SalaAltaDTO("Sala 1 - IMAX", TipoSala.IMAX, 8, 12));
        Sala sala2 = salaService.altaSalaConMapa(new SalaAltaDTO("Sala 2 - 3D Atmos", TipoSala.SALA_3D, 7, 10));
        Sala sala3 = salaService.altaSalaConMapa(new SalaAltaDTO("Sala 3 - VIP Motion", TipoSala.VIP, 5, 8));

        LocalDate hoy = LocalDate.now();
        LocalDate manana = hoy.plusDays(1);
        LocalDate proximoFinde = proximoSabadoODomingo(hoy);

        funcion(sala1.getId(), 1L, hoy.atTime(19, 0), "8500");
        funcion(sala1.getId(), 1L, hoy.atTime(21, 45), "8500");
        funcion(sala2.getId(), 1L, manana.atTime(20, 0), "7000");
        funcion(sala1.getId(), 1L, proximoFinde.atTime(22, 0), "9500");

        funcion(sala2.getId(), 2L, hoy.atTime(18, 30), "7000");
        funcion(sala3.getId(), 2L, manana.atTime(19, 15), "11000");
        funcion(sala2.getId(), 2L, proximoFinde.atTime(17, 0), "7500");

        funcion(sala3.getId(), 3L, hoy.atTime(22, 0), "11000");
        funcion(sala3.getId(), 3L, proximoFinde.atTime(20, 30), "12000");

        log.info("Sembradas 3 salas y 9 funciones (hoy/mañana/finde) en modulo-salas");
    }

    private void funcion(Long salaId, Long peliculaId, LocalDateTime fechaHora, String precio) {
        funcionService.crearFuncion(new FuncionAltaDTO(salaId, peliculaId, fechaHora, new BigDecimal(precio)));
    }

    private LocalDate proximoSabadoODomingo(LocalDate desde) {
        LocalDate d = desde;
        while (d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY) {
            d = d.plusDays(1);
        }
        return d;
    }
}
