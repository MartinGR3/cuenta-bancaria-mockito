package com.academia.banco;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Donde viven las cuentas (en producción sería una base de datos).
 * Hoy NO tiene implementación: en las pruebas la reemplazas por un doble.
 */
public interface RepositorioCuentas {

    Optional<CuentaBancaria> buscar(String numeroCuenta);

    /** Cuánto se ha retirado de esa cuenta en ese día (sin contar comisiones). */
    BigDecimal totalRetiradoEn(String numeroCuenta, LocalDate fecha);

    void guardar(String numeroCuenta, CuentaBancaria cuenta);
}
