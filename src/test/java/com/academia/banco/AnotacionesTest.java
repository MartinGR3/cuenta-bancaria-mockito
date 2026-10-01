package com.academia.banco;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("MP-4 · Lo mismo, con @Mock y @Captor")
class AnotacionesTest {

    @Mock
    private RepositorioCuentas repositorio;
    @Mock
    private ServicioAntifraude antifraude;
    @Mock
    private Notificador notificador;
    @Captor
    private ArgumentCaptor<String> mensaje;

    private CajeroAutomatico cajero;
    private CuentaBancaria cuenta;

    @BeforeEach
    void preparar() {
        cajero = new CajeroAutomatico(repositorio, antifraude, notificador, Clock.systemDefaultZone());
        cuenta = new CuentaBancaria("Ana");
        cuenta.depositar(new BigDecimal("2000.00"));
    }

    @Test
    @DisplayName("el SMS dice el monto y el saldo que queda")
    void textoDelSms() {
        when(repositorio.buscar("001")).thenReturn(Optional.of(cuenta));
        when(repositorio.totalRetiradoEn(eq("001"), any(LocalDate.class))).thenReturn(new BigDecimal("0.00"));

        cajero.retirar("001", new BigDecimal("500.00"));

        verify(notificador).enviarSms(eq("001"), mensaje.capture());
        assertEquals("Retiro de $500.00. Saldo: $1500.00", mensaje.getValue());
    }

    @Test
    @DisplayName("con una cuenta que no existe, ni antifraude ni SMS")
    void cuentaNoExiste() {
        when(repositorio.buscar("999")).thenReturn(Optional.empty());
        assertThrows(CuentaNoEncontradaException.class,() -> cajero.retirar("999", new BigDecimal("500.00")));

        verifyNoInteractions(antifraude, notificador);
    }

    @Test
    @DisplayName("una operación sospechosa se rechaza y la cuenta no cambia")
    void sospechosa() {
        when(repositorio.buscar("001")).thenReturn(Optional.of(cuenta));
        when(antifraude.esSospechosa("001", new BigDecimal("500.00"))).thenReturn(true);
        assertThrows(OperacionRechazadaException.class,() -> cajero.retirar("001", new BigDecimal("500.00")));

        assertEquals(new BigDecimal("2000.00"), cuenta.getSaldo());
    }
}