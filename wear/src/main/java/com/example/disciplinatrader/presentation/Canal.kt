package com.example.disciplinatrader.presentation

interface Canal {
    var alCambiarConexion: (Boolean) -> Unit
    var alRecibir: (Mensaje) -> Unit
    fun iniciar()
    fun enviar(mensaje: Mensaje)
    fun detener()
}