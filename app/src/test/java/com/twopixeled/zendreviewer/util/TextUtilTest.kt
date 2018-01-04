package com.twopixeled.zendreviewer.util

import org.junit.Test
import org.junit.Assert.assertEquals

class TextUtilTest {
    private val textUtil = TextUtil()

    @Test
    fun lessThanTagsAreDecoded() {
        assertEquals(
                "<This is less than tag",
                textUtil.formatAnswerOpts("&lt;This is less than tag")
        )
    }

    @Test
    fun greaterThanTagsAreDecoded() {
        assertEquals(
                "This is greater than tag>",
                textUtil.formatAnswerOpts("This is greater than tag&gt;")
        )
    }

    @Test
    fun lessThanTagsWithoutSemicolonsAreDecoded() {
        assertEquals(
                "<This is less than tag",
                textUtil.formatAnswerOpts("&ltThis is less than tag")
        )
    }

    @Test
    fun greaterThanTagsWithoutSemicolonsAreDecoded() {
        assertEquals(
                "This is greater than tag>",
                textUtil.formatAnswerOpts("This is greater than tag&gt")
        )
    }

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