package com.example.disciplinatrader.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    private var conectado by mutableStateOf(false)
    private var ultimoMensaje by mutableStateOf("(ninguno)")
    private val canal: Canal = CanalSocketCliente()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        canal.alCambiarConexion = { conectado = it }
        canal.alRecibir = { ultimoMensaje = it.aLinea() }
        canal.iniciar()

        setContent {
            val texto = TextStyle(color = Color.White, fontSize = 12.sp)
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BasicText("Celular: ${if (conectado) "conectado" else "sin conexión"}", style = texto)
                BasicText("Recibí: $ultimoMensaje", style = texto)
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Boton("Confirmar", Color(0xFF2E7D32), texto) {
                        canal.enviar(Mensaje(TipoMensaje.CONFIRMAR))
                    }
                    Boton("Cancelar", Color(0xFFC62828), texto) {
                        canal.enviar(Mensaje(TipoMensaje.CANCELAR))
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        canal.detener()
    }
}

@Composable
private fun Boton(texto: String, color: Color, estilo: TextStyle, alTocar: () -> Unit) {
    Box(
        modifier = Modifier
            .background(color)
            .clickable { alTocar() }
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        BasicText(texto, style = estilo)
    }
}