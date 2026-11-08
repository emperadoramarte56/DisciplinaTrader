package com.example.disciplinatrader

/** Regla del freno emocional. Sin dependencias de Android, para poder probarla con pruebas unitarias. */
class FrenoEmocional(
    val pulsoBase: Int = 70,
    val umbralPorcentaje: Int = 20,
    val segundosPausa: Int = 10
) {
    /** Pulso a partir del cual (estrictamente mayor) se activa el freno. Con los valores por defecto: 84. */
    val umbral: Int get() = pulsoBase + pulsoBase * umbralPorcentaje / 100

    fun requiereFreno(pulso: Int): Boolean = pulso > umbral
}