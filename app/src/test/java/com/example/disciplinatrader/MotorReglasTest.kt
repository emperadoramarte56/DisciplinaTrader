package com.example.disciplinatrader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MotorReglasTest {

    private var ahora = 0L
    private fun motor() = MotorReglas(reloj = { ahora })

    @Test
    fun sinPerdidasNoHayBloqueo() {
        val m = motor()
        m.registrarResultado(50.0)
        assertFalse(m.estaBloqueado())
    }

    @Test
    fun dosPerdidasPequenasNoBloquean() {
        val m = motor()
        m.registrarResultado(-10.0)
        m.registrarResultado(-10.0)
        assertFalse(m.estaBloqueado())
    }

    @Test
    fun tresPerdidasSeguidasBloquean() {
        val m = motor()
        m.registrarResultado(-10.0)
        m.registrarResultado(-10.0)
        val activo = m.registrarResultado(-10.0)
        assertTrue(activo)
        assertTrue(m.estaBloqueado())
    }

    @Test
    fun unaGananciaReiniciaLaRacha() {
        val m = motor()
        m.registrarResultado(-10.0)
        m.registrarResultado(-10.0)
        m.registrarResultado(5.0)
        m.registrarResultado(-10.0)
        m.registrarResultado(-10.0)
        assertFalse(m.estaBloqueado())
    }

    @Test
    fun perdidaDiariaMayorAl3PorCientoBloquea() {
        val m = motor()
        m.registrarResultado(-400.0)
        assertTrue(m.estaBloqueado())
    }

    @Test
    fun elBloqueoTerminaDespuesDeLaDuracion() {
        val m = motor()
        m.registrarResultado(-400.0)
        assertEquals(60L, m.segundosRestantes())
        ahora = 59_999L
        assertTrue(m.estaBloqueado())
        ahora = 60_000L
        assertFalse(m.estaBloqueado())
    }

    @Test
    fun levantarBloqueoDesbloquea() {
        val m = motor()
        m.registrarResultado(-400.0)
        m.levantarBloqueo()
        assertFalse(m.estaBloqueado())
    }
}