package com.example.upcampusplus.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidationTest {
    @Test
    fun `T-F1 titulo vazio ou so com espacos e invalido`() {
        assertNull(normalizeTitle(""))
        assertNull(normalizeTitle("    "))
        assertNull(normalizeTitle("\n\t"))
    }

    @Test
    fun `RF-004 titulo e gravado sem espacos nas pontas`() {
        assertEquals("Estudar ViewModel", normalizeTitle("  Estudar ViewModel  "))
    }

    @Test
    fun `T-F2 colar 150 caracteres deixa 100`() {
        val pasted = "a".repeat(150)
        val limited = limitTitleInput(pasted)
        assertEquals(TITLE_MAX_LENGTH, limited.length)
        assertEquals(limited, normalizeTitle(limited))
    }

    @Test
    fun `RN-01 limites de 1 e 100 caracteres`() {
        assertEquals("a", normalizeTitle("a"))
        assertEquals(100, normalizeTitle("b".repeat(100))?.length)
        assertNull(normalizeTitle("b".repeat(101)))
    }
}
