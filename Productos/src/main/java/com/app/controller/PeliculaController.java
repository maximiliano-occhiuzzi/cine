package com.app.controller;

import com.app.dto.PeliculaAltaDTO;
import com.app.dto.PeliculaModificarDTO;
import com.app.dto.PeliculaSalidaDTO;
import com.app.service.PeliculaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/productos/peliculas")
@RequiredArgsConstructor
public class PeliculaController {

    private final PeliculaService peliculaService;

    @PostMapping
    public ResponseEntity<PeliculaSalidaDTO> crear(@Valid @RequestBody PeliculaAltaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(peliculaService.crearPelicula(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PeliculaSalidaDTO> modificar(@PathVariable Long id,
                                                        @Valid @RequestBody PeliculaModificarDTO dto) {
        return ResponseEntity.ok(peliculaService.modificarPelicula(id, dto));
    }

    @GetMapping
    public ResponseEntity<List<PeliculaSalidaDTO>> listarCartelera() {
        return ResponseEntity.ok(peliculaService.listarCartelera());
    }

    @GetMapping("/todas")
    public ResponseEntity<List<PeliculaSalidaDTO>> listarTodas() {
        return ResponseEntity.ok(peliculaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PeliculaSalidaDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(peliculaService.obtenerPorId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        peliculaService.eliminarPelicula(id);
        return ResponseEntity.noContent().build();
    }
}
