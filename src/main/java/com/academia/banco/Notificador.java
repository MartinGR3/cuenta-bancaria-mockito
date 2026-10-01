package com.academia.banco;

/**
 * Manda un SMS al cliente. En producción cuesta dinero y necesita internet.
 * Hoy NO tiene implementación: en las pruebas la reemplazas por un doble.
 */
public interface Notificador {

    void enviarSms(String numeroCuenta, String mensaje);
}
