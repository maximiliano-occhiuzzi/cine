package com.app.service;

import com.app.dto.ProductoAltaDTO;
import com.app.dto.ProductoModificarDTO;
import com.app.dto.ProductoSalidaDTO;
import com.app.enums.CategoriaProducto;
import com.app.exception.ProductoDuplicadoException;
import com.app.exception.ProductoInactivoException;
import com.app.exception.RecursoNoEncontradoException;
import com.app.exception.StockInsuficienteException;
import com.app.model.Producto;
import com.app.repository.ProductoRepository;
import com.app.specification.ProductoSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;

    @Override
    public ProductoSalidaDTO altaProducto(ProductoAltaDTO dto) {
        if (productoRepository.existsByNombreIgnoreCase(dto.nombre().trim())) {
            throw new ProductoDuplicadoException("Ya existe un producto con el nombre: " + dto.nombre());
        }

        Producto producto = new Producto();
        producto.setNombre(dto.nombre().trim());
        producto.setDescripcion(dto.descripcion());
        producto.setPrecio(dto.precio());
        producto.setStock(dto.stock());
        producto.setCategoria(dto.categoria());
        producto.setImagenUrl(dto.imagenUrl());
        producto.setActivo(true);

        return ProductoSalidaDTO.from(productoRepository.save(producto));
    }

    @Override
    public ProductoSalidaDTO modificarProducto(Long id, ProductoModificarDTO dto) {
        Producto existente = obtenerEntidad(id);

        if (productoRepository.existsByNombreIgnoreCaseAndIdNot(dto.nombre().trim(), id)) {
            throw new ProductoDuplicadoException("Ya existe otro producto con el nombre: " + dto.nombre());
        }

        existente.setNombre(dto.nombre().trim());
        existente.setDescripcion(dto.descripcion());
        existente.setPrecio(dto.precio());
        existente.setCategoria(dto.categoria());
        existente.setImagenUrl(dto.imagenUrl());

        return ProductoSalidaDTO.from(productoRepository.save(existente));
    }

    @Override
    public Page<ProductoSalidaDTO> listarProductos(CategoriaProducto categoria, String nombre,
                                                   boolean incluirInactivos, Pageable pageable) {
        Specification<Producto> spec = (root, query, cb) -> cb.conjunction();

        if (!incluirInactivos) {
            spec = spec.and(ProductoSpecifications.activo(true));
        }
        if (categoria != null) {
            spec = spec.and(ProductoSpecifications.categoria(categoria));
        }
        if (nombre != null && !nombre.isBlank()) {
            spec = spec.and(ProductoSpecifications.nombreContiene(nombre.trim()));
        }

        return productoRepository.findAll(spec, pageable).map(ProductoSalidaDTO::from);
    }

    @Override
    public ProductoSalidaDTO obtenerPorId(Long id) {
        return ProductoSalidaDTO.from(obtenerEntidad(id));
    }

    @Override
    public List<ProductoSalidaDTO> listarBajoStock(int umbral) {
        return productoRepository.findBajoStock(umbral)
                .stream()
                .map(ProductoSalidaDTO::from)
                .toList();
    }

    @Override
    @Transactional
    public ProductoSalidaDTO ajustarStock(Long id, Integer cantidad) {
        Producto producto = obtenerEntidad(id);

        if (Boolean.FALSE.equals(producto.getActivo())) {
            throw new ProductoInactivoException(
                    "No se puede ajustar el stock de un producto dado de baja: " + producto.getNombre());
        }

        int nuevoStock = producto.getStock() + cantidad;
        if (nuevoStock < 0) {
            throw new StockInsuficienteException(
                    "Stock insuficiente para '" + producto.getNombre() + "'. Stock actual: "
                            + producto.getStock() + ", ajuste solicitado: " + cantidad);
        }

        producto.setStock(nuevoStock);
        return ProductoSalidaDTO.from(productoRepository.saveAndFlush(producto));
    }

    @Override
    public ProductoSalidaDTO reactivarProducto(Long id) {
        Producto producto = obtenerEntidad(id);
        producto.setActivo(true);
        return ProductoSalidaDTO.from(productoRepository.save(producto));
    }

    @Override
    public void eliminarProducto(Long id) {
        Producto producto = obtenerEntidad(id);
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    private Producto obtenerEntidad(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con id: " + id));
    }
}
