package com.academia.banco;

import java.util.ArrayList;
import java.util.List;

/** Un doble HECHO A MANO: en vez de mandar SMS, los anota en una lista para que la prueba los revise. */
class NotificadorFalso implements Notificador {

    final List<String> enviados = new ArrayList<>();

    @Override
    public void enviarSms(String numeroCuenta, String mensaje) {
        enviados.add(numeroCuenta + ": " + mensaje);
    }
}