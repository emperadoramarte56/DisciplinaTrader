package com.example.disciplinatrader

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import kotlin.concurrent.thread

class CanalSocketServidor(private val puerto: Int = 8080) : Canal {

    override var alCambiarConexion: (Boolean) -> Unit = {}
    override var alRecibir: (Mensaje) -> Unit = {}

    @Volatile private var salida: PrintWriter? = null
    private var servidor: ServerSocket? = null

    override fun iniciar() {
        thread(isDaemon = true) {
            try {
                val server = ServerSocket(puerto)
                servidor = server
                while (!server.isClosed) {
                    val socket = server.accept()
                    salida = PrintWriter(socket.getOutputStream(), true)
                    alCambiarConexion(true)
                    try {
                        val lector = BufferedReader(InputStreamReader(socket.getInputStream()))
                        while (true) {
                            val linea = lector.readLine() ?: break
                            Mensaje.deLinea(linea)?.let { alRecibir(it) }
                        }
                    } catch (_: Exception) {
                    }
                    salida = null
                    alCambiarConexion(false)
                    socket.close()
                }
            } catch (_: Exception) {
                alCambiarConexion(false)
            }
        }
    }

    override fun enviar(mensaje: Mensaje) {
        thread { salida?.println(mensaje.aLinea()) }
    }

    override fun detener() {
        servidor?.close()
    }
}