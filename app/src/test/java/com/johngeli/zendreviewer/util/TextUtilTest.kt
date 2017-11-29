package com.johngeli.zendreviewer.util

import org.junit.Test
import org.junit.Assert.assertEquals

class TextUtilTest {
    private val textUtil = TextUtil()

    @Test
    fun preTagsAreRemoved() {
        assertEquals(
                "Sample code here",
                textUtil.formatAnswerOpts("<pre>Sample code here</pre>")
        )
    }

    @Test
    fun newLinesAreReplacedWithBrTags() {
        assertEquals(
                "Hello<br>World!",
                textUtil.formatHTMLToAndroid("Hello\nWorld!")
        )
    }

    @Test
    fun preTagsAreReplacedWithCodeTags() {
        assertEquals(
                "<code>code here</code>",
                textUtil.formatHTMLToAndroid("<pre>code here</pre>")
        )
    }
}