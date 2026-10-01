package com.academia.banco;

public class OperacionRechazadaException extends RuntimeException {

    public OperacionRechazadaException(String motivo) {
        super("Operación rechazada: " + motivo);
    }
}
