package it.softsamurai.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FrenchGradeTest {

    private fun grade(text: String) =
        requireNotNull(FrenchGrade.parse(text)) { "Not a grade: $text" }

    @Test
    fun `grades are ordered by number, letter and plus`() {
        val ordered = listOf("4c", "5a", "5c+", "6a", "6a+", "6b", "6c+", "7a", "8b+", "9c+")

        assertEquals(ordered, ordered.shuffled().sortedBy { grade(it) })
    }

    @Test
    fun `plus is harder than the plain grade and easier than the next letter`() {
        assertTrue(grade("6a") < grade("6a+"))
        assertTrue(grade("6a+") < grade("6b"))
        assertTrue(grade("5c+") < grade("6a"))
    }

    @Test
    fun `toString writes the grade back`() {
        listOf("3a", "5c+", "6b", "7a+", "9c+").forEach { assertEquals(it, grade(it).toString()) }
    }

    @Test
    fun `free text keeps the first grade`() {
        assertEquals("6a", grade("6a/6a+").toString())
        assertEquals("7b", grade(" 7b (proj)").toString())
        assertEquals("6c", grade("6C").toString())
    }

    @Test
    fun `number without letter counts as its a`() {
        assertEquals(grade("4a"), grade("4"))
    }

    @Test
    fun `unknown grades are null`() {
        assertNull(FrenchGrade.parse("?"))
        assertNull(FrenchGrade.parse(""))
        assertNull(FrenchGrade.parse("n.d."))
    }

    @Test
    fun `bands split at 6a, 7a and 8a`() {
        assertEquals(GradeBand.UpTo5, GradeBand.of(grade("5c+")))
        assertEquals(GradeBand.Six, GradeBand.of(grade("6a")))
        assertEquals(GradeBand.Six, GradeBand.of(grade("6c+")))
        assertEquals(GradeBand.Seven, GradeBand.of(grade("7a")))
        assertEquals(GradeBand.EightPlus, GradeBand.of(grade("8a")))
        assertEquals(GradeBand.EightPlus, GradeBand.of(grade("9a")))
    }
}
