package com.amebo.core.converter

import junit.framework.Assert.assertEquals
import org.junit.Test

class ConverterTest {
    companion object {
        init {
          DocConverter.initialize()
        }
    }

    @Test
    fun `test conversion works as expected`() {
        val inputs = arrayOf(
            "[quote] This is Nigeria[/quote]",
            "[color=#990000]Please every Programmer should try as much as possible to check on this thread once in a while to help answer some questions Newbies would like to ask.[/color]"
        )
        val outputs = arrayOf(
            "<blockquote> This is Nigeria</blockquote>",
            "<span style=\"color:#990000\">Please every Programmer should try as much as possible to check on this thread once in a while to help answer some questions Newbies would like to ask.</span>"
        )
        inputs.forEachIndexed { index, input ->
            assertEquals(outputs[index], DocConverter.bbCodeToHtml(input))
        }

        println( DocConverter.bbCodeToHtml("[quote][b]dfsdf[/b][/quote]"))

    }
}