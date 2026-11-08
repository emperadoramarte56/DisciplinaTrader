package com.example.disciplinatrader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiarioTest {

    @Test
    fun elMotivoVaALaUltimaCompraSinMotivo() {
        val d = Diario()
        d.registrarCompra("USD/MXN", 70, false)
        d.registrarCompra("EUR/USD", 90, true)
        d.asignarMotivo("Intuicion")
        assertEquals(null, d.operaciones()[0].motivo)
        assertEquals("Intuicion", d.operaciones()[1].motivo)
    }

    @Test
    fun sinComprasNoSePuedeAsignarMotivo() {
        assertFalse(Diario().asignarMotivo("Noticia"))
    }

    @Test
    fun cerrarPosicionRepartePorIgualEntreLasComprasAbiertas() {
        val d = Diario()
        d.registrarCompra("USD/MXN", 70, false)
        d.registrarCompra("USD/MXN", 72, false)
        d.cerrarPosicion("USD/MXN", 20.0)
        assertEquals(10.0, d.operaciones()[0].resultado!!, 0.0001)
        assertEquals(10.0, d.operaciones()[1].resultado!!, 0.0001)
    }

    @Test
    fun cerrarUnParNoTocaLasComprasDeOtroPar() {
        val d = Diario()
        d.registrarCompra("USD/MXN", 70, false)
        d.registrarCompra("EUR/USD", 70, false)
        d.cerrarPosicion("USD/MXN", 5.0)
        assertEquals(null, d.operaciones()[1].resultado)
    }

    @Test
    fun elResumenAgrupaPorMotivo() {
        val d = Diario()
        d.registrarCompra("EUR/USD", 95, true); d.asignarMotivo("Intuicion"); d.cerrarPosicion("EUR/USD", -50.0)
        d.registrarCompra("EUR/USD", 70, false); d.asignarMotivo("Analisis"); d.cerrarPosicion("EUR/USD", 30.0)
        d.registrarCompra("EUR/USD", 71, false); d.asignarMotivo("Analisis"); d.cerrarPosicion("EUR/USD", -10.0)

        val resumen = d.resumenPorMotivo()
        val analisis = resumen.first { it.motivo == "Analisis" }
        val intuicion = resumen.first { it.motivo == "Intuicion" }

        assertEquals(2, analisis.cerradas)
        assertEquals(1, analisis.ganadas)
        assertEquals(50, analisis.porcentajeGanadas)
        assertEquals(20.0, analisis.total, 0.0001)
        assertEquals(-50.0, intuicion.total, 0.0001)
    }

    @Test
    fun lasOperacionesAbiertasNoCuentanEnElResumen() {
        val d = Diario()
        d.registrarCompra("USD/MXN", 70, false)
        d.asignarMotivo("Noticia")
        assertTrue(d.resumenPorMotivo().isEmpty())
    }

    @Test
    fun sinMotivoSeAgrupaComoSinMotivo() {
        val d = Diario()
        d.registrarCompra("USD/MXN", 70, false)
        d.cerrarPosicion("USD/MXN", 5.0)
        assertEquals("Sin motivo", d.resumenPorMotivo()[0].motivo)
    }
}