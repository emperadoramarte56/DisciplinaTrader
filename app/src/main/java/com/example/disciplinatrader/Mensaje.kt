package com.example.disciplinatrader

enum class TipoMensaje {
    // celular -> reloj
    ORDEN_PENDIENTE,
    INICIAR_PAUSA,
    BLOQUEO_ACTIVO,
    BLOQUEO_LEVANTADO,
    // reloj -> celular
    CONFIRMAR,
    CANCELAR,
    MOTIVO,
    PULSO,
    DESBLOQUEAR
}

data class Mensaje(val tipo: TipoMensaje, val dato: String = "") {

    /** Convierte el mensaje a una línea de texto, por ejemplo "MOTIVO|Intuicion". */
    fun aLinea(): String = "${tipo.name}|$dato"

    companion object {
        /** Convierte una línea recibida en un Mensaje. Devuelve null si no es válida. */
        fun deLinea(linea: String): Mensaje? {
            val partes = linea.split("|", limit = 2)
            val tipo = TipoMensaje.entries.firstOrNull { it.name == partes[0] } ?: return null
            return Mensaje(tipo, partes.getOrElse(1) { "" })
        }
    }
}