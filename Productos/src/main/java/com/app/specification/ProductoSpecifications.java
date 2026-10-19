package com.app.specification;

import com.app.enums.CategoriaProducto;
import com.app.model.Producto;
import org.springframework.data.jpa.domain.Specification;


public final class ProductoSpecifications {

    private ProductoSpecifications() {
    }

    public static Specification<Producto> activo(boolean activo) {
        return (root, query, cb) -> cb.equal(root.get("activo"), activo);
    }

    public static Specification<Producto> categoria(CategoriaProducto categoria) {
        return (root, query, cb) -> cb.equal(root.get("categoria"), categoria);
    }

    public static Specification<Producto> nombreContiene(String nombre) {
        String patron = "%" + nombre.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("nombre")), patron);
    }
}
