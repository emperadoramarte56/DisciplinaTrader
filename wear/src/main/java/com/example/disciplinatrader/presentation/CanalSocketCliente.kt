package com.example.disciplinatrader.presentation

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import kotlin.concurrent.thread

class CanalSocketCliente(
    private val host: String = "10.0.2.2",
    private val puerto: Int = 8080
) : Canal {

    override var alCambiarConexion: (Boolean) -> Unit = {}
    override var alRecibir: (Mensaje) -> Unit = {}

    @Volatile private var salida: PrintWriter? = null
    @Volatile private var activo = true

    override fun iniciar() {
        thread(isDaemon = true) {
            while (activo) {
                try {
                    val socket = Socket(host, puerto)
                    salida = PrintWriter(socket.getOutputStream(), true)
                    alCambiarConexion(true)
                    val lector = BufferedReader(InputStreamReader(socket.getInputStream()))
                    while (true) {
                        val linea = lector.readLine() ?: break
                        Mensaje.deLinea(linea)?.let { alRecibir(it) }
                    }
                    socket.close()
                } catch (_: Exception) {
                }
                salida = null
                alCambiarConexion(false)
                Thread.sleep(2000)
            }
        }
    }

    override fun enviar(mensaje: Mensaje) {
        thread { salida?.println(mensaje.aLinea()) }
    }

    override fun detener() {
        activo = false
    }
}