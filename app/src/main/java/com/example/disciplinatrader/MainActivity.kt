package com.example.disciplinatrader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("Celular (servidor)")
                Text("Reloj: ${if (conectado) "conectado" else "sin conexión"}")
                Text("Último mensaje: $ultimoMensaje")
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