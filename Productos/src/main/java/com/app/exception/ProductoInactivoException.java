package com.app.exception;


public class ProductoInactivoException extends RuntimeException {

    public ProductoInactivoException(String mensaje) {
        super(mensaje);
    }
}
