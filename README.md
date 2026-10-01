# Cajero automático — pruebas con JUnit 5 y Mockito

**Autor:** Martin Gonzalez Rico

## Cómo correr las pruebas

    ./probar.sh final

## Qué prueba cada clase

| Clase de prueba | Qué prueba | Qué dobles usa | Cuántas pruebas |
|---|---|---|---|
| CajeroConDoblesAManoTest | El retiro exitoso (baja el saldo, guarda la cuenta y manda un SMS) y que una cuenta inexistente se rechaza sin SMS | fakes hechos a mano y una lambda | 2 |
| StubsTest | Que el mock responde lo que yo decido: retiro normal, consultar saldo, cuenta inexistente, operación sospechosa, límite diario, repositorio caído y un mock sin configurar ( Optional.empty ) |  mock()  con  when(...).thenReturn(...)  y  thenThrow(...)  | 7 |
| InteraccionesTest | Qué se llamó, cuántas veces y en qué orden: el texto del SMS, guardar una vez con un SMS, el orden antifraude → guardar → SMS, que el SMS falle sin deshacer el retiro, y que ante una cuenta inexistente, una operación sospechosa o un saldo insuficiente no se guarde ni se mande SMS |  mock()  con  verify ,  times ,  never ,  verifyNoInteractions ,  InOrder  y  thenThrow  | 7 |
| AnotacionesTest | Lo mismo que MP-2 y MP-3 (operación sospechosa, texto del SMS, cuenta inexistente) pero con anotaciones |  @Mock  y  @Captor  ( ArgumentCaptor ) | 3 |
| QuejasCajeroTest | Las 5 quejas, cada una reproducida con una prueba: SMS sin saldo, operación sospechosa que mueve dinero, guardado doble, antifraude caído y límite diario exacto de $8,000.00 |  mock()  sin la extensión, con dos stubs comunes en el  @BeforeEach ;  verify ,  verifyNoInteractions ,  never  y  times  | 5 |
| RelojTest | El límite diario según la fecha del reloj del cajero, incluida la zona horaria de CDMX frente a UTC y el cambio de día |  Clock.fixed  (reloj real, no un mock) y  mock()  para repositorio, antifraude y notificador | 3 |

## Boleto de salida

1. **¿Qué diferencia hay entre un stub y un mock? Da un ejemplo de cada uno de hoy.**
   Un stub es un doble que entrega datos a lo que estoy probando: le digo qué responder y no me importa cuántas veces lo llamaron. Un mock es un doble al que le verifico el comportamiento: reviso qué se le llamó, cuántas veces y en qué orden. Es el mismo objeto de Mockito; cambia el papel que juega en la prueba.
   - Stub:  when(repositorio.buscar("001")).thenReturn(Optional.of(cuenta))  (le doy al cajero la cuenta que debe encontrar).
   - Mock:  verify(repositorio, times(1)).guardar("001", cuenta)  o  verifyNoInteractions(notificador)  (compruebo qué hizo el cajero con sus colaboradores).

2. **¿Por qué la prueba inútil de la MP-6 salía en verde con el cajero roto?**
   Porque mockeaba justo lo que debía probar: la  CuentaBancaria . La prueba le decía al mock  when(cuentaFalsa.getSaldo()).thenReturn(new BigDecimal("1500.00"))  y al final comprobaba que  consultarSaldo  devolvía  1500.00 . Eso solo confirma que el mock devuelve lo que yo mismo le dije. Con el cajero roto (sin la línea  cuenta.retirar(monto); ) la prueba seguía en verde, porque  retirar  de un mock no hace nada y el saldo nunca depende del código real. Mientras tanto, mis otras pruebas, que usan una cuenta real, sí se ponían en rojo. Una prueba que no puede fallar es peor que ninguna: da confianza falsa.

3. **¿Qué NO mockearías en un proyecto de Spring Boot como el de la Semana 3, y por qué?**
   - La clase que estoy probando: si la mockeo, la prueba verifica el mock y no mi código.
   - Objetos de dominio y valores simples (como  CuentaBancaria , entidades, DTOs,  BigDecimal ): son baratos de crear de verdad, y en estas pruebas usé la cuenta real y así se comprueba su lógica.
   - Código que no es mío: Spring, JPA,  ObjectMapper , etc. Mockearlos obliga a adivinar su comportamiento y la prueba pasa aunque en producción falle.
   - El repositorio cuando lo que quiero probar son sus consultas ahí se prueba con una base de datos real (por ejemplo  @DataJpaTest ). Un repositorio mockeado solo devuelve lo que yo le dije y no prueba nada del SQL.
   Mockeo solo lo que está en la frontera y es lento, externo o impredecible (antifraude, SMS, servicios remotos).