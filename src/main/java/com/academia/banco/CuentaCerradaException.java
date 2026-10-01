package com.academia.banco;

public class CuentaCerradaException extends RuntimeException {

    public CuentaCerradaException(String titular) {
        super("La cuenta de " + titular + " está cerrada");
    }
}
