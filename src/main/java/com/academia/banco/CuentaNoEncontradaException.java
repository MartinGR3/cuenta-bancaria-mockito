package com.academia.banco;

public class CuentaNoEncontradaException extends RuntimeException {

    public CuentaNoEncontradaException(String numeroCuenta) {
        super("No existe la cuenta " + numeroCuenta);
    }
}
