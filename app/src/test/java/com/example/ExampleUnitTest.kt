package com.example

import com.example.ai.AiDslrStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testAiDslrStylesIntegrity() {
        val styles = AiDslrStyle.values()
        assertTrue("Must provide at least 5 DSLR styles", styles.size >= 5)

        val naturalStyle = AiDslrStyle.NATURAL_DSLR
        assertEquals("Natural DSLR", naturalStyle.displayName)
        assertTrue(naturalStyle.defaultSharpness > 0.5f)

        val portraitBokeh = AiDslrStyle.PORTRAIT_BOKEH
        assertTrue(portraitBokeh.defaultBlur >= 0.7f)
    }

    @Test
    fun testStylesHaveDisplayNames() {
        for (style in AiDslrStyle.values()) {
            assertNotNull(style.displayName)
            assertTrue(style.subtitle.isNotEmpty())
        }
    }
}
