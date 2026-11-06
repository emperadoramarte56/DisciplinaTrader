package com.example.disciplinatrader.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {

    private var estado by mutableStateOf("Conectando...")
    private var ultimoMensaje by mutableStateOf("(ninguno)")

    @Volatile private var salida: PrintWriter? = null
    @Volatile private var activo = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        conectar()

        setContent {
            val texto = TextStyle(color = Color.White, fontSize = 13.sp)
            Column(
                modifier = Modifier.fillMaxSize().padding(28.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BasicText("Reloj (cliente)", style = texto)
                BasicText("Estado: $estado", style = texto)
                BasicText("Celular dice: $ultimoMensaje", style = texto)
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .background(Color(0xFF3D5AFE))
                        .clickable { enviar("Hola desde el reloj") }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    BasicText("Enviar", style = texto)
                }
            }
        }
    }

    private fun conectar() {
        thread(isDaemon = true) {
            while (activo) {
                try {
                    val socket = Socket(HOST, PUERTO)
                    estado = "Conectado al celular"
                    salida = PrintWriter(socket.getOutputStream(), true)
                    val lector = BufferedReader(InputStreamReader(socket.getInputStream()))
                    while (true) {
                        val linea = lector.readLine() ?: break
                        ultimoMensaje = linea
                    }
                    socket.close()
                } catch (_: Exception) {
                }
                salida = null
                estado = "Sin conexión, reintentando..."
                Thread.sleep(2000)
            }
        }
    }

    private fun enviar(texto: String) {
        thread { salida?.println(texto) }
    }

    override fun onDestroy() {
        super.onDestroy()
        activo = false
    }

    companion object {
        const val HOST = "10.0.2.2"
        const val PUERTO = 8080
    }
}