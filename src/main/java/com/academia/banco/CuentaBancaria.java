package com.academia.banco;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Una cuenta de débito. Las REGLAS DEL BANCO (esto es lo que tus pruebas verifican):
 *
 *  1. La cuenta nace con saldo $0.00 y con un titular que no puede estar vacío.
 *  2. Todo monto (depósito, retiro, transferencia) debe ser mayor que cero
 *     y tener como máximo 2 decimales (no existen fracciones de centavo).
 *  3. Un retiro no puede pasar de $5,000.00 (límite por operación).
 *  4. Los 3 primeros retiros son gratis; desde el 4.º se cobra una comisión de $10.00.
 *  5. Si el saldo no alcanza para el retiro MÁS su comisión, se rechaza y la cuenta no cambia.
 *  6. Transferir no cobra comisión ni cuenta como retiro; si falla, ninguna de las dos cuentas cambia.
 *  7. Cada operación queda en el historial de movimientos (la comisión, como movimiento aparte).
 *     El historial se puede leer pero no modificar desde fuera.
 *  8. Solo se cierra una cuenta con saldo $0.00; cerrada, ya no admite operaciones
 *     (ni recibir transferencias) y no se puede volver a cerrar.
 */
public class CuentaBancaria {

    public static final BigDecimal LIMITE_POR_RETIRO = new BigDecimal("5000.00");
    public static final int RETIROS_GRATIS = 3;
    public static final BigDecimal COMISION = new BigDecimal("10.00");

    private final String titular;
    private BigDecimal saldo = new BigDecimal("0.00");
    private int retiros = 0;
    private final List<Movimiento> movimientos = new ArrayList<>();
    private boolean cerrada = false;

    public CuentaBancaria(String titular) {
        if (titular == null || titular.isBlank()) {
            throw new IllegalArgumentException("El titular es obligatorio");
        }
        this.titular = titular.strip();
    }

    public void depositar(BigDecimal monto) {
        validarAbierta();
        BigDecimal m = validarMonto(monto);
        saldo = saldo.add(m);
        movimientos.add(new Movimiento(TipoMovimiento.DEPOSITO, m));
    }

    public void retirar(BigDecimal monto) {
        validarAbierta();
        BigDecimal m = validarMonto(monto);
        if (m.compareTo(LIMITE_POR_RETIRO) > 0) {
            throw new LimiteExcedidoException(m, LIMITE_POR_RETIRO);
        }
        BigDecimal comision = comisionDelRetiro(retiros + 1);
        BigDecimal total = m.add(comision);
        if (total.compareTo(saldo) > 0) {
            throw new SaldoInsuficienteException(saldo, total);
        }
        saldo = saldo.subtract(total);
        retiros++;
        movimientos.add(new Movimiento(TipoMovimiento.RETIRO, m));
        if (comision.signum() > 0) {
            movimientos.add(new Movimiento(TipoMovimiento.COMISION, comision));
        }
    }

    public void transferir(BigDecimal monto, CuentaBancaria destino) {
        if (destino == null) {
            throw new IllegalArgumentException("La cuenta destino es obligatoria");
        }
        if (destino == this) {
            throw new IllegalArgumentException("No puedes transferirte a la misma cuenta");
        }
        validarAbierta();
        destino.validarAbierta();
        BigDecimal m = validarMonto(monto);
        if (m.compareTo(saldo) > 0) {
            throw new SaldoInsuficienteException(saldo, m);
        }
        saldo = saldo.subtract(m);
        destino.saldo = destino.saldo.add(m);
        movimientos.add(new Movimiento(TipoMovimiento.TRANSFERENCIA_ENVIADA, m));
        destino.movimientos.add(new Movimiento(TipoMovimiento.TRANSFERENCIA_RECIBIDA, m));
    }

    public void cerrar() {
        validarAbierta();
        if (saldo.signum() != 0) {
            throw new IllegalStateException("No puedes cerrar una cuenta con saldo: $" + saldo);
        }
        cerrada = true;
    }

    public boolean estaCerrada() {
        return cerrada;
    }

    /** Comisión del retiro número {@code numeroDeRetiro} (el primero es el 1). */
    public static BigDecimal comisionDelRetiro(int numeroDeRetiro) {
        if (numeroDeRetiro < 1) {
            throw new IllegalArgumentException("El número de retiro empieza en 1");
        }
        return numeroDeRetiro <= RETIROS_GRATIS ? new BigDecimal("0.00") : COMISION;
    }

    public String getTitular() {
        return titular;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public int getRetiros() {
        return retiros;
    }

    public List<Movimiento> getMovimientos() {
        return List.copyOf(movimientos);
    }

    private void validarAbierta() {
        if (cerrada) {
            throw new CuentaCerradaException(titular);
        }
    }

    private static BigDecimal validarMonto(BigDecimal monto) {
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero");
        }
        if (monto.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("El monto no puede tener fracciones de centavo");
        }
        return monto.setScale(2);
    }
}
