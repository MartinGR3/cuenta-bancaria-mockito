package com.academia.banco;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MP-1 · Cajero con dobles hechos a mano")
class CajeroConDoblesAManoTest {

    private RepositorioEnMemoria repositorio;
    private NotificadorFalso notificador;
    private CajeroAutomatico cajero;

    @BeforeEach
    void preparar() {
        repositorio = new RepositorioEnMemoria();
        notificador = new NotificadorFalso();
        ServicioAntifraude antifraudeQueTodoAprueba = (numeroCuenta, monto) -> false;
        cajero = new CajeroAutomatico(repositorio, antifraudeQueTodoAprueba, notificador, Clock.systemDefaultZone());

        CuentaBancaria cuenta = new CuentaBancaria("Ana");
        cuenta.depositar(new BigDecimal("2000.00"));
        repositorio.agregar("001", cuenta);
    }

    @Test
    @DisplayName("un retiro baja el saldo, guarda la cuenta y manda un SMS")
    void retiroExitoso() {
        cajero.retirar("001", new BigDecimal("500.00"));

        assertAll(
                () -> assertEquals(new BigDecimal("1500.00"), cajero.consultarSaldo("001")),
                () -> assertEquals(1, repositorio.vecesGuardado),
                () -> assertEquals(List.of("001: Retiro de $500.00. Saldo: $1500.00"), notificador.enviados));
    }
    @Test
    @DisplayName("una cuenta que no existe se rechaza y no manda SMS")
    void retirarSinExistir(){
        
        assertAll(
            ()-> assertThrows(CuentaNoEncontradaException.class, () -> cajero.retirar("999", new BigDecimal("500.00"))),
            ()-> assertTrue(notificador.enviados.isEmpty())
        );

    }
}