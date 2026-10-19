package com.app.exception;


public class ProductoDuplicadoException extends RuntimeException {

    public ProductoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
