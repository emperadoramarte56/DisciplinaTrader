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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

enum class Pantalla { ESPERA, PAUSA, DECIDIR, MOTIVO, BLOQUEO }

class MainActivity : ComponentActivity() {

    private var conectado by mutableStateOf(false)
    private var pantalla by mutableStateOf(Pantalla.ESPERA)
    private var pulso by mutableIntStateOf(0)
    private var simbolo by mutableStateOf("")
    private var pausaRestante by mutableIntStateOf(0)
    private var bloqueoRestante by mutableIntStateOf(0)
    private var bloqueoTranscurrido by mutableIntStateOf(0)

    private val canal: Canal = CanalSocketCliente()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        canal.alCambiarConexion = { conectado = it }
        canal.alRecibir = { procesarMensaje(it) }
        canal.iniciar()

        setContent {
            val texto = TextStyle(color = Color.White, fontSize = 13.sp)
            val chico = TextStyle(color = Color.White, fontSize = 11.sp)
            val grande = TextStyle(color = Color.White, fontSize = 22.sp)

            // Cuenta regresiva de la pausa y del bloqueo.
            LaunchedEffect(pantalla) {
                when (pantalla) {
                    Pantalla.PAUSA -> {
                        while (pausaRestante > 0) {
                            delay(1000)
                            pausaRestante--
                        }
                        pantalla = Pantalla.DECIDIR
                    }
                    Pantalla.BLOQUEO -> {
                        bloqueoTranscurrido = 0
                        while (bloqueoRestante > 0) {
                            delay(1000)
                            bloqueoRestante--
                            bloqueoTranscurrido++
                        }
                        pantalla = Pantalla.ESPERA
                    }
                    else -> {}
                }
            }

            Column(
                modifier = Modifier.fillMaxSize().background(Color.Black).padding(26.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (pantalla) {
                    Pantalla.ESPERA -> {
                        BasicText("Celular: ${if (conectado) "conectado" else "sin conexión"}", style = texto)
                        BasicText(
                            if (pulso > 0) "Pulso: $pulso lpm" else "Pulso: --",
                            style = grande
                        )
                        BasicText("Esperando órdenes", style = texto)
                    }

                    Pantalla.PAUSA -> {
                        val fase = if ((pausaRestante / 2) % 2 == 0) "Inhala..." else "Exhala..."
                        BasicText("Pulso alto: respira", style = texto)
                        BasicText(fase, style = grande)
                        BasicText("$pausaRestante s", style = grande)
                        BasicText(simbolo, style = texto)
                    }

                    Pantalla.DECIDIR -> {
                        BasicText("¿Comprar $simbolo?", style = texto)
                        Row(
                            modifier = Modifier.padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Boton("Confirmar", Color(0xFF2E7D32), chico) {
                                canal.enviar(Mensaje(TipoMensaje.CONFIRMAR))
                                pantalla = Pantalla.ESPERA
                            }
                            Boton("Cancelar", Color(0xFFC62828), chico) {
                                canal.enviar(Mensaje(TipoMensaje.CANCELAR))
                                pantalla = Pantalla.ESPERA
                            }
                        }
                    }

                    Pantalla.MOTIVO -> {
                        BasicText("¿Por qué compraste?", style = texto)
                        Row(
                            modifier = Modifier.padding(top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Boton("Análisis", Color(0xFF3D5AFE), chico) { elegirMotivo("Analisis") }
                            Boton("Noticia", Color(0xFF3D5AFE), chico) { elegirMotivo("Noticia") }
                        }
                        Row(
                            modifier = Modifier.padding(top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Boton("Intuición", Color(0xFF3D5AFE), chico) { elegirMotivo("Intuicion") }
                            Boton("Miedo", Color(0xFF3D5AFE), chico) { elegirMotivo("Miedo") }
                        }
                    }

                    Pantalla.BLOQUEO -> {
                        BasicText("BLOQUEADO", style = grande)
                        BasicText("$bloqueoRestante s", style = grande)
                        val espera = 10 - bloqueoTranscurrido
                        Boton(
                            texto = if (espera > 0) "Desbloquear ($espera)" else "Desbloquear",
                            color = Color(0xFF3D5AFE),
                            estilo = texto,
                            habilitado = espera <= 0
                        ) {
                            canal.enviar(Mensaje(TipoMensaje.DESBLOQUEAR))
                        }
                    }
                }
            }
        }
    }

    private fun elegirMotivo(motivo: String) {
        canal.enviar(Mensaje(TipoMensaje.MOTIVO, motivo))
        pantalla = Pantalla.ESPERA
    }

    private fun procesarMensaje(msg: Mensaje) {
        when (msg.tipo) {
            TipoMensaje.PULSO -> {
                pulso = msg.dato.toIntOrNull() ?: pulso
            }
            TipoMensaje.INICIAR_PAUSA -> {
                val partes = msg.dato.split(",")
                pausaRestante = partes.getOrNull(0)?.toIntOrNull() ?: 10
                simbolo = partes.getOrElse(1) { "" }
                pantalla = Pantalla.PAUSA
            }
            TipoMensaje.PEDIR_MOTIVO -> {
                simbolo = msg.dato
                pantalla = Pantalla.MOTIVO
            }
            TipoMensaje.BLOQUEO_ACTIVO -> {
                bloqueoRestante = msg.dato.toIntOrNull() ?: 60
                pantalla = Pantalla.BLOQUEO
            }
            TipoMensaje.BLOQUEO_LEVANTADO -> {
                pantalla = Pantalla.ESPERA
            }
            else -> {}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        canal.detener()
    }
}

@Composable
private fun Boton(
    texto: String,
    color: Color,
    estilo: TextStyle,
    habilitado: Boolean = true,
    alTocar: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(if (habilitado) color else Color(0xFF555555))
            .clickable(enabled = habilitado) { alTocar() }
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        BasicText(texto, style = estilo, softWrap = false, maxLines = 1)
    }
}