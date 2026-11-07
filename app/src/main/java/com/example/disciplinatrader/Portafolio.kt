package com.example.disciplinatrader

data class Posicion(val unidades: Double, val costo: Double)

/** Dinero ficticio: saldo, posiciones abiertas y resultados de operaciones cerradas. */
class Portafolio(
    val saldoInicial: Double = 10_000.0,
    val montoPorOperacion: Double = 1_000.0
) {
    var saldo: Double = saldoInicial
        private set

    private val posiciones = mutableMapOf<String, Posicion>()
    private val cerradas = mutableListOf<Double>()

    fun posicion(simbolo: String): Posicion? = posiciones[simbolo]

    /** Ganancia o pérdida de cada operación cerrada, en orden. */
    fun resultadosCerrados(): List<Double> = cerradas.toList()

    fun pnlRealizado(): Double = cerradas.sum()

    fun pnlAbierto(simbolo: String, precio: Double): Double {
        val pos = posiciones[simbolo] ?: return 0.0
        return pos.unidades * precio - pos.costo
    }

    /** Devuelve null si salió bien, o un texto con el motivo del rechazo. */
    fun comprar(simbolo: String, precio: Double): String? {
        if (saldo < montoPorOperacion) return "Saldo insuficiente"
        val actual = posiciones[simbolo]
        posiciones[simbolo] = Posicion(
            unidades = (actual?.unidades ?: 0.0) + montoPorOperacion / precio,
            costo = (actual?.costo ?: 0.0) + montoPorOperacion
        )
        saldo -= montoPorOperacion
        return null
    }

    /** Cierra toda la posición del par. Devuelve null si salió bien. */
    fun vender(simbolo: String, precio: Double): String? {
        val pos = posiciones[simbolo] ?: return "No tienes posición en $simbolo"
        val valor = pos.unidades * precio
        saldo += valor
        cerradas.add(valor - pos.costo)
        posiciones.remove(simbolo)
        return null
    }
}