package com.example.disciplinatrader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.Locale

class MainActivity : ComponentActivity() {

    private var conectado by mutableStateOf(false)
    private var ultimoMensaje by mutableStateOf("(ninguno)")
    private var aviso by mutableStateOf("")
    private var version by mutableIntStateOf(0)
    private var pulso by mutableIntStateOf(70)
    private var pendiente by mutableStateOf<String?>(null)
    private var cancelaciones by mutableIntStateOf(0)

    private val canal: Canal = CanalSocketServidor()
    private val generador = GeneradorPrecios()
    private val portafolio = Portafolio()
    private val motor = MotorReglas(capitalInicial = portafolio.saldoInicial)
    private val freno = FrenoEmocional()
    private val diario = Diario()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        canal.alCambiarConexion = {
            conectado = it
            if (it) {
                // Al conectarse el reloj, le mandamos el pulso actual
                canal.enviar(Mensaje(TipoMensaje.PULSO, pulso.toString()))
            } else {
                pendiente = null
            }
        }
        canal.alRecibir = { msg ->
            ultimoMensaje = msg.aLinea()
            runOnUiThread { procesarMensaje(msg) }
        }
        canal.iniciar()

        setContent {
            var pares by remember { mutableStateOf(generador.pares()) }
            var ahora by remember { mutableLongStateOf(System.currentTimeMillis()) }

            LaunchedEffect(Unit) {
                while (true) {
                    delay(1000)
                    pares = generador.siguiente()
                    ahora = System.currentTimeMillis()
                }
            }

            // Leer estos estados obliga a redibujar cuando cambian.
            @Suppress("UNUSED_VARIABLE") val refrescoVersion = version
            @Suppress("UNUSED_VARIABLE") val refrescoTiempo = ahora

            val bloqueado = motor.estaBloqueado()
            val hayPendiente = pendiente != null

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text("Mercado de divisas (simulado)")
                Text("Reloj: ${if (conectado) "conectado" else "sin conexión"}")
                Text("Saldo: $${formato(portafolio.saldo)}")
                Text("Resultado cerrado: $${formato(portafolio.pnlRealizado())}")
                Text("Órdenes frenadas (canceladas): $cancelaciones")

                if (bloqueado) {
                    Text(
                        "BLOQUEADO: quedan ${motor.segundosRestantes()} s",
                        color = Color(0xFFC62828)
                    )
                }
                if (hayPendiente) {
                    Text(
                        "Esperando decisión en el reloj: ${pendiente}",
                        color = Color(0xFFEF6C00)
                    )
                }

                pares.forEach { par ->
                    val pos = portafolio.posicion(par.simbolo)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(par.simbolo)
                            Text(
                                par.precioTexto(),
                                color = if (par.sube) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                            if (pos != null) {
                                Text("Abierta: $${formato(portafolio.pnlAbierto(par.simbolo, par.precio))}")
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                enabled = !bloqueado && !hayPendiente,
                                onClick = { intentarComprar(par) }
                            ) { Text("Comprar") }
                            Button(
                                enabled = pos != null,
                                onClick = {
                                    val error = portafolio.vender(par.simbolo, par.precio)
                                    aviso = error ?: "Cerraste ${par.simbolo}"
                                    if (error == null) {
                                        val resultado = portafolio.resultadosCerrados().last()
                                        diario.cerrarPosicion(par.simbolo, resultado)
                                        registrarResultado(resultado)
                                    }
                                    version++
                                }
                            ) { Text("Vender") }
                        }
                    }
                }

                Text(aviso)

                Text("— PATRONES POR MOTIVO —")
                val resumen = diario.resumenPorMotivo()
                if (resumen.isEmpty()) {
                    Text("Aún no hay operaciones cerradas.")
                } else {
                    resumen.forEach { r ->
                        Text(
                            "${r.motivo}: ${r.cerradas} cerradas, " +
                                    "${r.porcentajeGanadas}% ganadas, total $${formato(r.total)}",
                            color = if (r.total >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    }
                }

                Text("— DEBUG —")
                Text("Pulso simulado: $pulso lpm (el freno se activa arriba de ${freno.umbral})")
                Slider(
                    value = pulso.toFloat(),
                    onValueChange = { pulso = it.toInt() },
                    onValueChangeFinished = {
                        canal.enviar(Mensaje(TipoMensaje.PULSO, pulso.toString()))
                    },
                    valueRange = 50f..140f
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        registrarResultado(-100.0)
                        version++
                    }) { Text("Forzar pérdida") }
                    Button(onClick = {
                        motor.levantarBloqueo()
                        canal.enviar(Mensaje(TipoMensaje.BLOQUEO_LEVANTADO))
                        aviso = "Bloqueo levantado (debug)"
                        version++
                    }) { Text("Levantar") }
                }
                Button(onClick = {
                    generarEjemplos()
                    version++
                }) { Text("Generar datos de ejemplo") }
                Text("Último mensaje del reloj: $ultimoMensaje")
            }
        }
    }

    /** Compra directa si el pulso es normal; si es alto, manda la orden al reloj para la pausa. */
    private fun intentarComprar(par: ParDivisas) {
        if (motor.estaBloqueado()) {
            aviso = "Bloqueado: no puedes comprar ahora"
        } else if (freno.requiereFreno(pulso)) {
            if (!conectado) {
                aviso = "Pulso alto: conecta el reloj para poder confirmar"
            } else {
                pendiente = par.simbolo
                canal.enviar(
                    Mensaje(
                        TipoMensaje.INICIAR_PAUSA,
                        "${freno.segundosPausa},${par.simbolo},COMPRA"
                    )
                )
                aviso = "Pulso alto ($pulso): pausa en el reloj"
            }
        } else {
            ejecutarCompra(par.simbolo, huboPausa = false)
        }
        version++
    }

    /** Ejecuta la compra, la registra en el diario y le pide el motivo al reloj. */
    private fun ejecutarCompra(simbolo: String, huboPausa: Boolean) {
        val par = generador.pares().first { it.simbolo == simbolo }
        val error = portafolio.comprar(par.simbolo, par.precio)
        if (error != null) {
            aviso = error
            return
        }
        diario.registrarCompra(simbolo, pulso, huboPausa)
        aviso = "Compraste $simbolo"
        if (conectado) {
            canal.enviar(Mensaje(TipoMensaje.PEDIR_MOTIVO, simbolo))
        }
    }

    private fun procesarMensaje(msg: Mensaje) {
        when (msg.tipo) {
            TipoMensaje.CONFIRMAR -> {
                val simbolo = pendiente
                pendiente = null
                if (simbolo != null) {
                    if (motor.estaBloqueado()) {
                        aviso = "Orden rechazada: hay un bloqueo activo"
                    } else {
                        ejecutarCompra(simbolo, huboPausa = true)
                    }
                }
            }
            TipoMensaje.CANCELAR -> {
                if (pendiente != null) {
                    cancelaciones++
                    aviso = "Cancelaste la orden de $pendiente desde el reloj"
                }
                pendiente = null
            }
            TipoMensaje.MOTIVO -> {
                if (diario.asignarMotivo(msg.dato)) {
                    aviso = "Motivo registrado: ${msg.dato}"
                }
            }
            TipoMensaje.DESBLOQUEAR -> {
                motor.levantarBloqueo()
                canal.enviar(Mensaje(TipoMensaje.BLOQUEO_LEVANTADO))
                aviso = "Bloqueo levantado desde el reloj"
            }
            else -> {}
        }
        version++
    }

    /** Pasa el resultado al motor y, si activa el bloqueo, avisa al reloj. */
    private fun registrarResultado(resultado: Double) {
        if (motor.registrarResultado(resultado)) {
            aviso = "Bloqueo activado por ${motor.segundosRestantes()} s"
            canal.enviar(
                Mensaje(TipoMensaje.BLOQUEO_ACTIVO, motor.segundosRestantes().toString())
            )
        }
    }

    /** Datos falsos para ver las estadísticas sin esperar. Usan un par aparte ("EJEMPLO"). */
    private fun generarEjemplos() {
        val datos = listOf(
            Triple("Analisis", 72, 25.0), Triple("Analisis", 70, 18.0), Triple("Analisis", 75, -12.0),
            Triple("Noticia", 80, 8.0), Triple("Noticia", 78, -20.0),
            Triple("Intuicion", 92, -35.0), Triple("Intuicion", 96, -28.0), Triple("Intuicion", 88, 10.0),
            Triple("Miedo", 105, -40.0), Triple("Miedo", 99, -22.0)
        )
        datos.forEach { (motivo, p, resultado) ->
            diario.registrarCompra("EJEMPLO", p, p > freno.umbral)
            diario.asignarMotivo(motivo)
            diario.cerrarPosicion("EJEMPLO", resultado)
        }
        aviso = "Datos de ejemplo generados"
    }

    private fun formato(valor: Double): String = String.format(Locale.US, "%.2f", valor)

    override fun onDestroy() {
        super.onDestroy()
        canal.detener()
    }
}