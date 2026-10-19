package com.app.config;

import com.app.enums.CategoriaProducto;
import com.app.model.Producto;
import com.app.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductoRepository productoRepository;

    @Override
    public void run(String... args) {
        if (productoRepository.count() > 0) {
            return;
        }

        List<Producto> productos = List.of(
                producto("Combo Mega Pochoclo + 2 Gaseosas",
                        "1 Balde de pochoclos gigantes (dulces o salados) + 2 gaseosas grandes de 1L.",
                        "14500", 40, CategoriaProducto.COMBO,
                        "https://images.unsplash.com/photo-1585647347384-2593bc35786b?q=80&w=600&auto=format&fit=crop"),

                producto("Pochoclo Grande",
                        "Balde grande de pochoclos recién hechos.",
                        "6500", 60, CategoriaProducto.COMIDA,
                        "https://images.unsplash.com/photo-1578849278619-e73505e9610f?q=80&w=600&auto=format&fit=crop"),

                producto("Nacho Supreme con Queso Cheddar",
                        "Caja de nachos crocantes con salsa de queso cheddar caliente y jalapeños.",
                        "8900", 35, CategoriaProducto.COMIDA,
                        "https://images.unsplash.com/photo-1582169296194-e4d644c48063?q=80&w=600&auto=format&fit=crop"),

                producto("Gaseosa Grande 1 Litro",
                        "Coca-Cola, Coca Zero, Sprite o Fanta helada de 1000ml.",
                        "4500", 80, CategoriaProducto.BEBIDA,
                        "https://images.unsplash.com/photo-1554866585-cd94860890b7?q=80&w=600&auto=format&fit=crop"),

                producto("Agua Mineral 500ml",
                        "Botella de agua mineral sin gas, bien fría.",
                        "2500", 100, CategoriaProducto.BEBIDA,
                        "https://images.unsplash.com/photo-1560023907-5f339617ea30?q=80&w=600&auto=format&fit=crop"),

                producto("Vaso Coleccionable Resident Evil",
                        "Vaso de edición limitada de 1.2L con figura 3D en la tapa.",
                        "12000", 25, CategoriaProducto.MERCHANDISING,
                        "https://images.unsplash.com/photo-1536440136628-849c177e76a1?q=80&w=600&auto=format&fit=crop")
        );

        productoRepository.saveAll(productos);
        log.info("Cargados {} productos predeterminados en modulo-productos", productos.size());
    }

    private Producto producto(String nombre, String descripcion, String precio,
                              int stock, CategoriaProducto categoria, String imagenUrl) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setPrecio(new BigDecimal(precio));
        p.setStock(stock);
        p.setCategoria(categoria);
        p.setImagenUrl(imagenUrl);
        p.setActivo(true);
        return p;
    }
}
