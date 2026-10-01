package com.academia.banco;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

/**
 * Un cajero automático. Usa tres servicios que NO son suyos (repositorio, antifraude, SMS) y un reloj.
 * REGLAS DEL CAJERO (esto es lo que tus pruebas verifican):
 *
 *  C1. Consultar el saldo de una cuenta que existe devuelve su saldo; si no existe: CuentaNoEncontradaException.
 *  C2. Para retirar, primero se busca la cuenta. Si no existe: CuentaNoEncontradaException,
 *      y no se consulta al antifraude ni se manda SMS.
 *  C3. Justo después de encontrar la cuenta (ANTES de revisar el límite y de tocarla) se consulta al antifraude.
 *      Si es sospechosa, O SI EL ANTIFRAUDE NO RESPONDE, se rechaza con OperacionRechazadaException:
 *      la cuenta no cambia, no se guarda y no se manda SMS.
 *  C4. Límite diario: lo retirado hoy (según el repositorio) más el nuevo retiro no puede pasar de $8,000.00.
 *      Si pasa: LimiteDiarioExcedidoException, y nada cambia. «Hoy» es la fecha que marca el reloj del cajero.
 *  C5. Si la cuenta rechaza el retiro (saldo insuficiente, límite por operación, cuenta cerrada),
 *      su excepción sale tal cual: no se guarda y no se manda SMS.
 *  C6. Si el retiro procede: la cuenta se guarda UNA vez y DESPUÉS se manda UN SMS a esa cuenta con el texto
 *      «Retiro de $<monto>. Saldo: $<saldo>».
 *  C7. Si el SMS falla, el retiro YA se hizo y NO se deshace: el cajero no lanza ninguna excepción.
 *  C8. Si el repositorio no responde (ServicioNoDisponibleException), esa excepción sale tal cual.
 */
public class CajeroAutomatico {

    public static final BigDecimal LIMITE_DIARIO = new BigDecimal("8000.00");

    private final RepositorioCuentas repositorio;
    private final ServicioAntifraude antifraude;
    private final Notificador notificador;
    private final Clock reloj;

    public CajeroAutomatico(RepositorioCuentas repositorio, ServicioAntifraude antifraude,
                            Notificador notificador, Clock reloj) {
        this.repositorio = repositorio;
        this.antifraude = antifraude;
        this.notificador = notificador;
        this.reloj = reloj;
    }

    public BigDecimal consultarSaldo(String numeroCuenta) {
        return buscar(numeroCuenta).getSaldo();
    }

    public void retirar(String numeroCuenta, BigDecimal monto) {
        CuentaBancaria cuenta = buscar(numeroCuenta);
        validarAntifraude(numeroCuenta, monto);
        validarLimiteDiario(numeroCuenta, monto);
        cuenta.retirar(monto);
        repositorio.guardar(numeroCuenta, cuenta);
        avisar(numeroCuenta, "Retiro de $" + monto + ". Saldo: $" + cuenta.getSaldo());
    }

    private CuentaBancaria buscar(String numeroCuenta) {
        return repositorio.buscar(numeroCuenta)
                .orElseThrow(() -> new CuentaNoEncontradaException(numeroCuenta));
    }

    private void validarAntifraude(String numeroCuenta, BigDecimal monto) {
        boolean sospechosa;
        try {
            sospechosa = antifraude.esSospechosa(numeroCuenta, monto);
        } catch (ServicioNoDisponibleException e) {
            throw new OperacionRechazadaException("el antifraude no responde");
        }
        if (sospechosa) {
            throw new OperacionRechazadaException("operación sospechosa");
        }
    }

    private void validarLimiteDiario(String numeroCuenta, BigDecimal monto) {
        LocalDate hoy = LocalDate.now(reloj);
        BigDecimal yaRetirado = repositorio.totalRetiradoEn(numeroCuenta, hoy);
        if (yaRetirado.add(monto).compareTo(LIMITE_DIARIO) > 0) {
            throw new LimiteDiarioExcedidoException(yaRetirado, monto, LIMITE_DIARIO);
        }
    }

    private void avisar(String numeroCuenta, String mensaje) {
        try {
            notificador.enviarSms(numeroCuenta, mensaje);
        } catch (RuntimeException e) {
            // C7: el SMS es un aviso, no parte de la operación. El retiro ya se hizo y no se deshace.
        }
    }
}
