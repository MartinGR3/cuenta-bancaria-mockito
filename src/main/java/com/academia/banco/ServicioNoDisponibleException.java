package com.academia.banco;

public class ServicioNoDisponibleException extends RuntimeException {

    public ServicioNoDisponibleException(String servicio) {
        super("El servicio " + servicio + " no responde");
    }
}
