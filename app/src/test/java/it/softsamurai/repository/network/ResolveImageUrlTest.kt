package it.softsamurai.repository.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResolveImageUrlTest {

    @Test
    fun `absolute https URL is returned unchanged`() {
        val url = "https://www.falesiaonline.it/uploads/assets/image/photo.jpg"
        assertEquals(url, resolveImageUrl(url))
    }

    @Test
    fun `absolute http URL is returned unchanged`() {
        val url = "http://www.falesiaonline.it/uploads/assets/image/photo.jpg"
        assertEquals(url, resolveImageUrl(url))
    }

    @Test
    fun `root-relative URL is resolved against base host`() {
        assertEquals(
            "https://www.falesiaonline.it/uploads/assets/image/photo.jpg",
            resolveImageUrl("/uploads/assets/image/photo.jpg"),
        )
    }

    @Test
    fun `non-root-relative URL gets host and slash prepended`() {
        assertEquals(
            "https://www.falesiaonline.it/images/photo.jpg",
            resolveImageUrl("images/photo.jpg"),
        )
    }

    @Test
    fun `blank src returns null`() {
        assertNull(resolveImageUrl(""))
    }

    @Test
    fun `whitespace-only src returns null`() {
        // orEmpty() + isNotBlank() guard — a src that is all spaces is treated as missing
        assertNull(resolveImageUrl("   "))
    }
}
