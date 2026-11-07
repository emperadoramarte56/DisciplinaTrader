package com.example.disciplinatrader

import java.util.Locale
import kotlin.random.Random

data class ParDivisas(
    val simbolo: String,
    val precio: Double,
    val anterior: Double,
    val decimales: Int
) {
    val sube: Boolean get() = precio >= anterior

    fun precioTexto(): String = String.format(Locale.US, "%.${decimales}f", precio)
}

/** Genera precios simulados con un movimiento aleatorio pequeño (random walk). */
class GeneradorPrecios(private val random: Random = Random.Default) {

    private var lista = listOf(
        ParDivisas("USD/MXN", 18.5000, 18.5000, 4),
        ParDivisas("EUR/USD", 1.0800, 1.0800, 4),
        ParDivisas("GBP/USD", 1.2700, 1.2700, 4),
        ParDivisas("USD/JPY", 150.00, 150.00, 2)
    )

    fun pares(): List<ParDivisas> = lista

    /** Avanza un "tick": cada par cambia hasta ±0.15 %. */
    fun siguiente(): List<ParDivisas> {
        lista = lista.map { p ->
            val cambio = (random.nextDouble() - 0.5) * 0.003
            ParDivisas(p.simbolo, p.precio * (1 + cambio), p.precio, p.decimales)
        }
        return lista
    }
}

