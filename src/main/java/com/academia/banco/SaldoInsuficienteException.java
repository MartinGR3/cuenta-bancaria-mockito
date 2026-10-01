package com.academia.banco;

import java.math.BigDecimal;

public class SaldoInsuficienteException extends RuntimeException {

    public SaldoInsuficienteException(BigDecimal saldo, BigDecimal necesario) {
        super("Saldo insuficiente: tienes $" + saldo + " y se necesitan $" + necesario);
    }
}
