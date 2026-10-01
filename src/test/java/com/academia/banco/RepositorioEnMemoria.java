package com.academia.banco;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Un doble HECHO A MANO: un repositorio que guarda las cuentas en un Map, en memoria. */
class RepositorioEnMemoria implements RepositorioCuentas {

    private final Map<String, CuentaBancaria> cuentas = new HashMap<>();
    int vecesGuardado = 0;

    void agregar(String numeroCuenta, CuentaBancaria cuenta) {
        cuentas.put(numeroCuenta, cuenta);
    }

    @Override
    public Optional<CuentaBancaria> buscar(String numeroCuenta) {
        return Optional.ofNullable(cuentas.get(numeroCuenta));
    }

    @Override
    public BigDecimal totalRetiradoEn(String numeroCuenta, LocalDate fecha) {
        return new BigDecimal("0.00");   // este doble no lleva la cuenta: siempre dice que hoy no se ha retirado nada
    }

    @Override
    public void guardar(String numeroCuenta, CuentaBancaria cuenta) {
        cuentas.put(numeroCuenta, cuenta);
        vecesGuardado++;
    }
}