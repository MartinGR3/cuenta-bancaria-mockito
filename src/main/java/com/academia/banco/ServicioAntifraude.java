package com.academia.banco;

import java.math.BigDecimal;

/**
 * Un servicio externo que decide si una operación es sospechosa.
 * Si no responde, lanza ServicioNoDisponibleException.
 * Hoy NO tiene implementación: en las pruebas la reemplazas por un doble.
 */
public interface ServicioAntifraude {

    boolean esSospechosa(String numeroCuenta, BigDecimal monto);
}
