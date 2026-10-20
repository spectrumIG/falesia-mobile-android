package it.softsamurai.resources

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Every user-facing string exists in English (`values/`) and Italian (`values-it/`), with the same keys
 * in the same order and the same format arguments. Plurals count as one entry per quantity used by both
 * languages (`name/one`, `name/other`).
 */
class StringResourcesParityTest {

    private val english = readStrings("values")
    private val italian = readStrings("values-it")

    @Test
    fun `english and italian define the same keys in the same order`() {
        assertEquals(english.keys.toList(), italian.keys.toList())
    }

    @Test
    fun `translations use the same format arguments`() {
        english.forEach { (key, value) ->
            assertEquals(
                "Format arguments of '$key'",
                formatArgs(value),
                formatArgs(italian.getValue(key))
            )
        }
    }

    @Test
    fun `no string is blank`() {
        (english + italian.mapKeys { "it/${it.key}" }).forEach { (key, value) ->
            assertTrue("'$key' is blank", value.isNotBlank())
        }
    }

    private fun formatArgs(value: String): List<String> =
        FORMAT_ARG.findAll(value).map { it.value }.sorted().toList()

    private fun readStrings(folder: String): Map<String, String> {
        // Unit tests run with the module directory as working directory.
        val file = File("src/main/res/$folder/strings.xml")
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        val strings = document.getElementsByTagName("string").elements()
            .associate { it.getAttribute("name") to it.textContent }
        val plurals = document.getElementsByTagName("plurals").elements().flatMap { plural ->
            plural.getElementsByTagName("item").elements()
                .filter { it.getAttribute("quantity") in SHARED_QUANTITIES }
                .map { "${plural.getAttribute("name")}/${it.getAttribute("quantity")}" to it.textContent }
        }
        return strings + plurals
    }

    private fun NodeList.elements(): List<Element> = (0 until length).map { item(it) as Element }

    private companion object {
        val FORMAT_ARG = Regex("""%(\d+\$)?[sd]""")

        /** Quantities English and Italian both use. */
        val SHARED_QUANTITIES = setOf("one", "other")
    }
}
