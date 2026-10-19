package com.app.config;

import com.app.model.Pelicula;
import com.app.repository.PeliculaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
@Order(2)
public class PeliculaDataInitializer implements CommandLineRunner {

    private final PeliculaRepository peliculaRepository;

    @Override
    public void run(String... args) {
        if (peliculaRepository.count() > 0) {
            return;
        }

        List<Pelicula> peliculas = List.of(
                pelicula("RESIDENT EVIL: NOCHE CERO", "Terror / Acción", 114, "Zach Cregger", null,
                        LocalDate.of(2025, 9, 25),
                        "Llega la aterradora reinvención de la saga de terror más aclamada de los " +
                                "videojuegos, ambientada en las oscuras instalaciones de la Corporación Umbrella.",
                        "R-17", 4.8,
                        "https://images.unsplash.com/photo-1536440136628-849c177e76a1?q=80&w=600&auto=format&fit=crop",
                        "Zachary Levi, Kaya Scodelario", "IMAX 3D / DBOX"),

                pelicula("SPIDER-MAN: BEYOND THE SPIDER-VERSE", "Animación / Acción", 140,
                        "Joaquim Dos Santos", null,
                        LocalDate.of(2026, 3, 12),
                        "Miles Morales emprende un viaje épico a través del Multiverso para salvar a sus " +
                                "seres queridos de una amenaza cataclísmica imprevista.",
                        "SAM 13", 4.9,
                        "https://images.unsplash.com/photo-1635805737707-575885ab0820?q=80&w=600&auto=format&fit=crop",
                        "Shameik Moore, Hailee Steinfeld", "2D / 3D / ATMOS"),

                pelicula("OPPENHEIMER", "Drama / Historia", 180, "Christopher Nolan", null,
                        LocalDate.of(2023, 7, 20),
                        "La historia del físico teórico J. Robert Oppenheimer y su papel fundamental en el " +
                                "desarrollo de las primeras armas nucleares.",
                        "SAM 16", 5.0,
                        "https://images.unsplash.com/photo-1440404653325-ab127d49abc1?q=80&w=600&auto=format&fit=crop",
                        "Cillian Murphy, Emily Blunt, Matt Damon", "IMAX 70MM / 2D")
        );

        peliculaRepository.saveAll(peliculas);
        log.info("Cargadas {} peliculas predeterminadas en modulo-productos", peliculas.size());
    }

    private Pelicula pelicula(String titulo, String genero, int duracion, String director,
                               String guionista, LocalDate fechaEstreno, String sinopsis,
                               String clasificacionEdad, double calificacion, String poster,
                               String reparto, String formato) {
        Pelicula p = new Pelicula();
        p.setTitulo(titulo);
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
        p.setFormatoDisponible(formato);
        p.setActivo(true);
        return p;
    }
}
