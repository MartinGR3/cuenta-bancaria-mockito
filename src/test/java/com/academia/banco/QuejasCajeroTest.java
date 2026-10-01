package com.academia.banco;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MP-5 · Quejas sobre el cajero (cada una reproducida con una prueba)")
class QuejasCajeroTest {

    private RepositorioCuentas repositorio;
    private ServicioAntifraude antifraude;
    private Notificador notificador;
    private CajeroAutomatico cajero;
    private CuentaBancaria cuenta;

    @BeforeEach
    void preparar() {
        repositorio = mock(RepositorioCuentas.class);
        antifraude = mock(ServicioAntifraude.class);
        notificador = mock(Notificador.class);
        cajero = new CajeroAutomatico(repositorio, antifraude, notificador, Clock.systemDefaultZone());
        cuenta = new CuentaBancaria("Ana");
        cuenta.depositar(new BigDecimal("2000.00"));
        // Lo común a todas las quejas: la cuenta 001 existe y hoy no se ha retirado nada
        when(repositorio.buscar("001")).thenReturn(Optional.of(cuenta));
        when(repositorio.totalRetiradoEn(eq("001"), any(LocalDate.class))).thenReturn(new BigDecimal("0.00"));
    }

    @Test
    @DisplayName("Queja 1: no llega SMS de un retiro que se rechazó por falta de saldo")
    void sinSmsSiNoHaySaldo() {
        assertThrows(SaldoInsuficienteException.class,() -> cajero.retirar("001", new BigDecimal("3000.00")));
        verifyNoInteractions(notificador);
    }

    @Test
    @DisplayName("Queja 2: si la operación es sospechosa, el dinero no sale de la cuenta")
    void sospechosaNoMueveDinero() {
        when(antifraude.esSospechosa(any(), any())).thenReturn(true);
        assertThrows(OperacionRechazadaException.class, () -> cajero.retirar("001", new BigDecimal("500.00")));
        assertEquals(new BigDecimal("2000.00"), cuenta.getSaldo());
    }

    @Test
    @DisplayName("Queja 3: cada retiro se guarda UNA sola vez")
    void seGuardaUnaVez() {
        cajero.retirar("001", new BigDecimal("500.00"));
        verify(repositorio, times(1)).guardar("001", cuenta);
    }

    @Test
    @DisplayName("Queja 4: si el antifraude no responde, el retiro se rechaza")
    void antifraudeCaidoRechaza() {
        when(antifraude.esSospechosa(any(), any())).thenThrow(new ServicioNoDisponibleException("antifraude"));
        assertThrows(OperacionRechazadaException.class,() -> cajero.retirar("001", new BigDecimal("500.00")));
        verify(repositorio, never()).guardar(any(), any());
    }

    @Test
    @DisplayName("Queja 5: se pueden retirar justo los $8,000.00 del día")
    void limiteDiarioExacto() {
        when(repositorio.totalRetiradoEn(eq("001"), any(LocalDate.class))).thenReturn(new BigDecimal("7500.00"));
        assertDoesNotThrow(() -> cajero.retirar("001", new BigDecimal("500.00")));
        assertEquals(new BigDecimal("1500.00"), cuenta.getSaldo());
    }
}