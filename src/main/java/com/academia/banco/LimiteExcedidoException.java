package com.academia.banco;

import java.math.BigDecimal;

public class LimiteExcedidoException extends RuntimeException {

    public LimiteExcedidoException(BigDecimal monto, BigDecimal limite) {
        super("El retiro de $" + monto + " pasa el límite de $" + limite + " por operación");
    }
}
