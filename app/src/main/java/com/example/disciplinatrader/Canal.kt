package com.example.disciplinatrader

/** Contrato de comunicación. Hoy usa sockets, mañana podría ser otra cosa. */
interface Canal {
    var alCambiarConexion: (Boolean) -> Unit
    var alRecibir: (Mensaje) -> Unit
    fun iniciar()
    fun enviar(mensaje: Mensaje)
    fun detener()
}