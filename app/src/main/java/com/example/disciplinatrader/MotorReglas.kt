package com.example.disciplinatrader

/** Reglas de disciplina. No depende de Android, así que se puede probar con pruebas unitarias. */
class MotorReglas(
    private val capitalInicial: Double = 10_000.0,
    private val maxPerdidasSeguidas: Int = 3,
    private val maxPerdidaDiariaPct: Double = 3.0,
    private val duracionCooldownMs: Long = 60_000L,
    private val reloj: () -> Long = { System.currentTimeMillis() }
) {
    private var perdidasSeguidas = 0
    private var resultadoDelDia = 0.0

    @Volatile private var bloqueadoHasta = 0L

    fun estaBloqueado(): Boolean = reloj() < bloqueadoHasta

    fun segundosRestantes(): Long {
        val resto = bloqueadoHasta - reloj()
        return if (resto <= 0) 0 else (resto + 999) / 1000
    }

    /** Registra el resultado de una operación cerrada. Devuelve true si ESTE resultado activó el bloqueo. */
    fun registrarResultado(resultado: Double): Boolean {
        resultadoDelDia += resultado
        if (resultado < 0) perdidasSeguidas++ else perdidasSeguidas = 0

        val limiteDiario = -capitalInicial * maxPerdidaDiariaPct / 100
        val porRacha = perdidasSeguidas >= maxPerdidasSeguidas
        val porPerdidaDiaria = resultado < 0 && resultadoDelDia <= limiteDiario

        if ((porRacha || porPerdidaDiaria) && !estaBloqueado()) {
            bloqueadoHasta = reloj() + duracionCooldownMs
            perdidasSeguidas = 0
            return true
        }
        return false
    }

    fun levantarBloqueo() {
        bloqueadoHasta = 0L
    }
}

