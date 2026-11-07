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
    private val canal: Canal = CanalSocketServidor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        canal.alCambiarConexion = { conectado = it }
        canal.alRecibir = { ultimoMensaje = it.aLinea() }
        canal.iniciar()

        setContent {
            val generador = remember { GeneradorPrecios() }
            val portafolio = remember { Portafolio() }
            var pares by remember { mutableStateOf(generador.pares()) }
            var version by remember { mutableIntStateOf(0) }
            var aviso by remember { mutableStateOf("") }

            LaunchedEffect(Unit) {
                while (true) {
                    delay(1000)
                    pares = generador.siguiente()
                }
            }

            // Leer 'version' obliga a redibujar cuando cambia el portafolio.
            @Suppress("UNUSED_VARIABLE")
            val refresco = version

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
                            Button(onClick = {
                                aviso = portafolio.comprar(par.simbolo, par.precio)
                                    ?: "Compraste ${par.simbolo}"
                                version++
                            }) { Text("Comprar") }
                            Button(
                                enabled = pos != null,
                                onClick = {
                                    aviso = portafolio.vender(par.simbolo, par.precio)
                                        ?: "Cerraste ${par.simbolo}"
                                    version++
                                }
                            ) { Text("Vender") }
                        }
                    }
                }

                Text(aviso)
                Text("Último mensaje del reloj: $ultimoMensaje")
            }
        }
    }

    private fun formato(valor: Double): String = String.format(Locale.US, "%.2f", valor)

    override fun onDestroy() {
        super.onDestroy()
        canal.detener()
    }
}