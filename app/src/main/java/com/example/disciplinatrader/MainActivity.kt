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
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

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
            var pares by remember { mutableStateOf(generador.pares()) }

            LaunchedEffect(Unit) {
                while (true) {
                    delay(1000)
                    pares = generador.siguiente()
                }
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("Mercado de divisas (simulado)")
                Text("Reloj: ${if (conectado) "conectado" else "sin conexión"}")

                pares.forEach { par ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(par.simbolo)
                        Text(
                            par.precioTexto(),
                            color = if (par.sube) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    }
                }

                Text("Último mensaje del reloj: $ultimoMensaje")
                Button(onClick = {
                    canal.enviar(Mensaje(TipoMensaje.ORDEN_PENDIENTE, "USD/MXN,COMPRA,1000"))
                }) {
                    Text("Simular orden")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        canal.detener()
    }
}