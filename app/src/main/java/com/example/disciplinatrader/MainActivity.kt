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

    private val canal: Canal = CanalSocketServidor()
    private val generador = GeneradorPrecios()
    private val portafolio = Portafolio()
    private val motor = MotorReglas(capitalInicial = portafolio.saldoInicial)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        canal.alCambiarConexion = { conectado = it }
        canal.alRecibir = { msg ->
            ultimoMensaje = msg.aLinea()
            if (msg.tipo == TipoMensaje.DESBLOQUEAR) {
                motor.levantarBloqueo()
                canal.enviar(Mensaje(TipoMensaje.BLOQUEO_LEVANTADO))
                aviso = "Bloqueo levantado desde el reloj"
                version++
            }
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

                if (bloqueado) {
                    Text(
                        "BLOQUEADO: quedan ${motor.segundosRestantes()} s",
                        color = Color(0xFFC62828)
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
                                enabled = !bloqueado,
                                onClick = {
                                    aviso = portafolio.comprar(par.simbolo, par.precio)
                                        ?: "Compraste ${par.simbolo}"
                                    version++
                                }
                            ) { Text("Comprar") }
                            Button(
                                enabled = pos != null,
                                onClick = {
                                    val error = portafolio.vender(par.simbolo, par.precio)
                                    aviso = error ?: "Cerraste ${par.simbolo}"
                                    if (error == null) {
                                        registrarResultado(portafolio.resultadosCerrados().last())
                                    }
                                    version++
                                }
                            ) { Text("Vender") }
                        }
                    }
                }

                Text(aviso)

                Text("— DEBUG —")
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
                Text("Último mensaje del reloj: $ultimoMensaje")
            }
        }
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

    private fun formato(valor: Double): String = String.format(Locale.US, "%.2f", valor)

    override fun onDestroy() {
        super.onDestroy()
        canal.detener()
    }
}