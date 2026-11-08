# DisciplinaTrader

Asistente de disciplina para traders, hecho para celular y smartwatch (Wear OS).
No te dice qué comprar: te ayuda a **no decidir con emoción**. Todo el dinero es
ficticio (simulador de divisas).

## El problema

Muchos traders novatos pierden por decisiones emocionales (operar en pánico,
intentar recuperar pérdidas) y no por falta de información. Esta app usa el
reloj para medir el estado del cuerpo y el celular para aplicar reglas.

## Funciones

1. **Freno emocional.** Si el pulso supera la base +20 % (más de 84 lpm con la
   configuración por defecto), la compra no se ejecuta de inmediato: el reloj
   muestra una pausa de 10 s con respiración guiada y después pide
   Confirmar o Cancelar.
2. **Cooldown tras pérdidas.** Tras 3 pérdidas seguidas, o una pérdida diaria
   mayor al 3 % del capital, se bloquean las compras 60 s. Se puede levantar
   desde el reloj tras una espera de 10 s. Vender sigue permitido.
3. **Diario de motivos.** Después de cada compra, el reloj pregunta el motivo
   (Análisis, Noticia, Intuición o Miedo). El celular muestra por motivo el
   número de operaciones cerradas, el % ganadas y el resultado total.

## Reparto de roles

| Dispositivo | Qué hace |
|---|---|
| Reloj (`wear`) | Muestra el pulso, guía la pausa, confirma/cancela, pide el motivo y permite desbloquear |
| Celular (`app`) | Mercado simulado, portafolio, motor de reglas, diario y estadísticas |

## Decisiones de diseño

- **Comunicación por socket local, no por Wearable Data Layer.** La Data Layer
  depende de Google Play Services y de emparejar emuladores con cuenta de
  Google. Para no depender de cuentas, el celular abre un servidor en el
  puerto 8080 y el reloj se conecta a `10.0.2.2:8080`. La interfaz `Canal`
  aísla esto: cambiar el transporte solo exigiría otra implementación.
- **Pulso simulado.** Los emuladores no tienen sensor cardíaco, así que un
  slider de la sección DEBUG del celular simula el pulso y lo envía al reloj.
  En un dispositivo real se leería con Health Services.
- **Precios simulados.** Cuatro pares (USD/MXN, EUR/USD, GBP/USD, USD/JPY)
  con movimiento aleatorio cada segundo, para que la demo no dependa de
  internet ni de claves de API.
- **Modo demo.** Pausa de 10 s y bloqueo de 60 s (valores pensados para
  presentar; se cambian en `FrenoEmocional.kt` y `MotorReglas.kt`).

## Requisitos

- Android Studio - Android Studio Rabbit 1 | 2026.2.1 (Build #AI-262.9437.185.2621.16467767)
- JDK incluido con Android Studio (JetBrains Runtime 25)
- Dos emuladores: un teléfono (por ejemplo Pixel 7, API 34) y un reloj
  (Wear OS Small Round, API 33)
- No se necesita cuenta de Google, Play Store ni claves de API

## Cómo ejecutarlo

1. Clona el repo y ábrelo en Android Studio. Espera la sincronización de Gradle.
2. Enciende los dos emuladores. Con `adb devices` comprueba sus nombres.
   El script asume `emulator-5554` para el celular; si el tuyo es otro,
   edita `scripts/conectar.bat`.
3. Ejecuta la configuración **`app`** en el emulador del celular.
4. Haz doble clic en **`scripts/conectar.bat`** (crea el puente de red).
5. Ejecuta la configuración **`wear`** en el emulador del reloj.
6. En el celular debe aparecer "Reloj: conectado".

Si deja de conectar (por ejemplo tras reiniciar un emulador), repite el paso 4.

## Guion de demo

1. Con el pulso en 70, pulsa **Comprar**: se ejecuta directo y el reloj pide el motivo.
2. Sube el slider a 95 y pulsa **Comprar**: el reloj muestra la pausa con
   respiración y luego Confirmar/Cancelar. Cancela una y confirma otra.
3. Pulsa **Forzar pérdida** 3 veces: se activa el bloqueo y el reloj muestra
   la cuenta regresiva. Desbloquea desde el reloj tras 10 s.
4. Pulsa **Generar datos de ejemplo** y revisa **Patrones por motivo**.

## Pruebas

La lógica de reglas no depende de Android y tiene pruebas unitarias
(`app/src/test`): motor de reglas, freno emocional y diario.
En Android Studio, clic derecho sobre la carpeta de tests > Run.

## Limitaciones conocidas

- El diario se guarda en memoria: se borra al cerrar la app (pendiente: Room).
- Pulso y precios son simulados.
- No es asesoría financiera: es una herramienta educativa con dinero ficticio.

## Estructura

```
app/      Celular: servidor, mercado, portafolio, reglas, diario, UI
wear/     Reloj: cliente, pantallas de pausa, decisión, motivo y bloqueo
scripts/  conectar.bat (adb forward 8080)
```