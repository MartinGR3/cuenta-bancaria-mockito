# Errores encontrados en el cajero del compañero

Mi suite (MP-1 a MP-4) atrapó sola **3** de los 5 errores: las pruebas que fallaron en mp5-companero.txt fueron InteraccionesTest.sinSaldo (queja 1), InteraccionesTest.sospechosaNoTocaNada (queja 2) y CajeroConDoblesAManoTest.retiroExitoso, InteraccionesTest.guardaConAviso, InteraccionesTest.orden e InteraccionesTest.smsFalla (queja 3). Las quejas 4 y 5 pasaron todas mis pruebas porque ninguna cubría un antifraude caído ni el límite diario exacto.

| Queja | Prueba que la reproduce | Qué estaba mal en el código (la línea y por qué) | ¿Mi suite ya lo atrapaba? |
|---|---|---|---|
| 1 | sinSmsSiNoHaySaldo | CajeroAutomatico.java:50: avisar(...)  se llamaba **antes** de  cuenta.retirar(monto)  (línea 51). Si la cuenta lanzaba  SaldoInsuficienteException , el SMS ya había salido. Además calculaba el saldo del SMS con  getSaldo().subtract(monto)  en vez de leerlo de la cuenta ya retirada. Viola C5 y C6 (el SMS va después de guardar). | **sí** ( InteraccionesTest.sinSaldo ) |
| 2 |  sospechosaNoMueveDinero  | Líneas 51–52:  cuenta.retirar(monto)  se ejecutaba **antes** de  validarAntifraude(...) . El dinero ya había salido cuando se rechazaba la operación, y la cuenta quedaba en 1500.00 en vez de 2000.00. Viola C3 (el antifraude va justo después de buscar la cuenta). | **sí** ( InteraccionesTest.sospechosaNoTocaNada ) |
| 3 |  seGuardaUnaVez  | Líneas 53 y 54:  repositorio.guardar(numeroCuenta, cuenta)  estaba escrito dos veces, así que cada retiro se guardaba dos veces. Viola C6 (guardar UNA vez). | **sí** ( retiroExitoso ,  guardaConAviso ,  orden  y  smsFalla ) |
| 4 |  antifraudeCaidoRechaza  | Línea 67: en  validarAntifraude , el  catch (ServicioNoDisponibleException e)  hacía  sospechosa = false , o sea que si el antifraude se caía, el retiro pasaba sin revisión. Debe rechazar con  OperacionRechazadaException . Viola C3. | **no** |
| 5 |  limiteDiarioExacto  | Línea 77: la comparación usaba  >= 0  en  validarLimiteDiario , por lo que 7,500.00 + 500.00 = 8,000.00 se rechazaba. La regla dice «no puede pasar de» 8,000.00, así que debe ser  > 0 . Viola C4. | **no** |

## Defecto extra (no estaba entre las 5 quejas)

En  retirar ,  validarLimiteDiario  (línea 49) se ejecutaba **antes** que el antifraude, cuando C3 pide el antifraude primero. Por eso  AnotacionesTest.sospechosa  y  StubsTest.sospechosa  fallaban con  NullPointerException : no configuran  totalRetiradoEn , el mock devuelve  null  y  yaRetirado.add(...)  explotaba (línea 77) antes de llegar al antifraude.