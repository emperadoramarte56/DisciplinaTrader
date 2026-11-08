package com.example.disciplinatrader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FrenoEmocionalTest {

    private val freno = FrenoEmocional()

    @Test
    fun elUmbralPorDefectoEs84() {
        assertEquals(84, freno.umbral)
    }

    @Test
    fun pulsoNormalNoActivaElFreno() {
        assertFalse(freno.requiereFreno(70))
    }

    @Test
    fun pulsoEnElUmbralExactoNoActivaElFreno() {
        assertFalse(freno.requiereFreno(84))
    }

    @Test
    fun pulsoPorEncimaDelUmbralActivaElFreno() {
        assertTrue(freno.requiereFreno(85))
        assertTrue(freno.requiereFreno(110))
    }
}