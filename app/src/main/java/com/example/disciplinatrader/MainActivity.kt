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
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {

    private var estado by mutableStateOf("Esperando al reloj...")
    private var ultimoMensaje by mutableStateOf("(ninguno)")

    @Volatile private var salida: PrintWriter? = null
    private var servidor: ServerSocket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        iniciarServidor()

        setContent {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("Celular (servidor)")
                Text("Estado: $estado")
                Text("Último mensaje del reloj: $ultimoMensaje")
                Button(onClick = { enviar("Hola desde el celular") }) {
                    Text("Enviar al reloj")
                }
            }
        }
    }

    private fun iniciarServidor() {
        thread(isDaemon = true) {
            try {
                val server = ServerSocket(PUERTO)
                servidor = server
                while (!server.isClosed) {
                    val socket = server.accept()
                    estado = "Reloj conectado"
                    salida = PrintWriter(socket.getOutputStream(), true)
                    val lector = BufferedReader(InputStreamReader(socket.getInputStream()))
                    try {
                        while (true) {
                            val linea = lector.readLine() ?: break
                            ultimoMensaje = linea
                        }
                    } catch (_: Exception) {
                    }
                    salida = null
                    estado = "Reloj desconectado, esperando..."
                    socket.close()
                }
            } catch (e: Exception) {
                estado = "Error: ${e.message}"
            }
        }
    }

    private fun enviar(texto: String) {
        // Escribir en un socket no se puede hacer en el hilo principal
        thread { salida?.println(texto) }
    }

    override fun onDestroy() {
        super.onDestroy()
        servidor?.close()
    }

    companion object {
        const val PUERTO = 8080
    }
}