package it.softsamurai.resources

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Every user-facing string exists in English (`values/`) and Italian (`values-it/`), with the same keys
 * in the same order and the same format arguments.
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
        val nodes = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
            .getElementsByTagName("string")
        return (0 until nodes.length).associate { index ->
            val element = nodes.item(index) as Element
            element.getAttribute("name") to element.textContent
        }
    }

    private companion object {
        val FORMAT_ARG = Regex("""%(\d+\$)?[sd]""")
    }
}
