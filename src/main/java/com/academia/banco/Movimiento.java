package com.academia.banco;

import java.math.BigDecimal;

/** Un renglón del estado de cuenta: qué pasó y por cuánto. */
public record Movimiento(TipoMovimiento tipo, BigDecimal monto) {
}
