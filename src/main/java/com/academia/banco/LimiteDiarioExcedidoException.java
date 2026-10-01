package com.academia.banco;

import java.math.BigDecimal;

public class LimiteDiarioExcedidoException extends RuntimeException {

    public LimiteDiarioExcedidoException(BigDecimal yaRetirado, BigDecimal monto, BigDecimal limite) {
        super("Hoy ya retiraste $" + yaRetirado + "; con $" + monto + " pasarías el límite diario de $" + limite);
    }
}
