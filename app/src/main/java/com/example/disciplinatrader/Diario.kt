package com.example.disciplinatrader
data class Operacion(
    val id: Int,
    val simbolo: String,
    val pulso: Int,
    val huboPausa: Boolean,
    val motivo: String? = null,
    val resultado: Double? = null
)

data class ResumenMotivo(
    val motivo: String,
    val cerradas: Int,
    val ganadas: Int,
    val total: Double
) {
    val porcentajeGanadas: Int
        get() = if (cerradas == 0) 0 else ganadas * 100 / cerradas
}

/** Registro de operaciones con su motivo. Sin dependencias de Android, para poder probarlo. */
class Diario {
    private val lista = mutableListOf<Operacion>()

    fun operaciones(): List<Operacion> = lista.toList()

    fun registrarCompra(simbolo: String, pulso: Int, huboPausa: Boolean): Operacion {
        val op = Operacion(lista.size + 1, simbolo, pulso, huboPausa)
        lista.add(op)
        return op
    }

    /** Asigna el motivo a la última compra que todavía no lo tiene. */
    fun asignarMotivo(motivo: String): Boolean {
        val i = lista.indexOfLast { it.motivo == null }
        if (i < 0) return false
        lista[i] = lista[i].copy(motivo = motivo)
        return true
    }

    /** Reparte el resultado entre las compras abiertas del par (todas usan el mismo monto). */
    fun cerrarPosicion(simbolo: String, resultadoTotal: Double) {
        val abiertas = lista.indices.filter {
            lista[it].simbolo == simbolo && lista[it].resultado == null
        }
        if (abiertas.isEmpty()) return
        val parte = resultadoTotal / abiertas.size
        abiertas.forEach { lista[it] = lista[it].copy(resultado = parte) }
    }

    /** Estadísticas por motivo, solo con operaciones ya cerradas. */
    fun resumenPorMotivo(): List<ResumenMotivo> =
        lista.filter { it.resultado != null }
            .groupBy { it.motivo ?: "Sin motivo" }
            .map { (motivo, ops) ->
                ResumenMotivo(
                    motivo = motivo,
                    cerradas = ops.size,
                    ganadas = ops.count { it.resultado!! > 0 },
                    total = ops.sumOf { it.resultado!! }
                )
            }
            .sortedBy { it.motivo }
}