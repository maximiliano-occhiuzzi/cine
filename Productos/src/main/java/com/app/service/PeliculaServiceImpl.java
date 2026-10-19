package com.app.service;

import com.app.dto.PeliculaAltaDTO;
import com.app.dto.PeliculaModificarDTO;
import com.app.dto.PeliculaSalidaDTO;
import com.app.exception.RecursoNoEncontradoException;
import com.app.model.Pelicula;
import com.app.repository.PeliculaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PeliculaServiceImpl implements PeliculaService {

    private final PeliculaRepository peliculaRepository;

    @Override
    @Transactional
    public PeliculaSalidaDTO crearPelicula(PeliculaAltaDTO dto) {
        Pelicula p = new Pelicula();
        volcar(dto.titulo(), dto.genero(), dto.duracion(), dto.director(), dto.guionista(),
                dto.fechaEstreno(), dto.sinopsis(), dto.clasificacionEdad(), dto.calificacion(),
                dto.poster(), dto.reparto(), dto.formatoDisponible(), p);
        p.setActivo(true);
        return PeliculaSalidaDTO.from(peliculaRepository.save(p));
    }

    @Override
    @Transactional
    public PeliculaSalidaDTO modificarPelicula(Long id, PeliculaModificarDTO dto) {
        Pelicula p = obtenerEntidad(id);
        volcar(dto.titulo(), dto.genero(), dto.duracion(), dto.director(), dto.guionista(),
                dto.fechaEstreno(), dto.sinopsis(), dto.clasificacionEdad(), dto.calificacion(),
                dto.poster(), dto.reparto(), dto.formatoDisponible(), p);
        return PeliculaSalidaDTO.from(peliculaRepository.save(p));
    }

    @Override
    public List<PeliculaSalidaDTO> listarCartelera() {
        return peliculaRepository.findByActivoTrue().stream().map(PeliculaSalidaDTO::from).toList();
    }

    @Override
    public List<PeliculaSalidaDTO> listarTodas() {
        return peliculaRepository.findAll().stream().map(PeliculaSalidaDTO::from).toList();
    }

    @Override
    public PeliculaSalidaDTO obtenerPorId(Long id) {
        return PeliculaSalidaDTO.from(obtenerEntidad(id));
    }

    @Override
    @Transactional
    public void eliminarPelicula(Long id) {
        Pelicula p = obtenerEntidad(id);
        p.setActivo(false);
        peliculaRepository.save(p);
    }

    private void volcar(String titulo, String genero, Integer duracion, String director,
                         String guionista, LocalDate fechaEstreno, String sinopsis,
                         String clasificacionEdad, Double calificacion, String poster,
                         String reparto, String formatoDisponible, Pelicula p) {
        p.setTitulo(titulo.trim());
        p.setGenero(genero);
        p.setDuracion(duracion);
        p.setDirector(director);
        p.setGuionista(guionista);
        p.setFechaEstreno(fechaEstreno);
        p.setSinopsis(sinopsis);
        p.setClasificacionEdad(clasificacionEdad);
        p.setCalificacion(calificacion);
        p.setPoster(poster);
        p.setReparto(reparto);
        p.setFormatoDisponible(formatoDisponible);
    }

    private Pelicula obtenerEntidad(Long id) {
        return peliculaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pelicula no encontrada con id: " + id));
    }
}
