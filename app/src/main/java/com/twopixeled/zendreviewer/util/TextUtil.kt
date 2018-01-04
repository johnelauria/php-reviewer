package com.twopixeled.zendreviewer.util

/**
 * Utility class for formatting texts
 */
class TextUtil {
    fun formatAnswerOpts(answer: String): String {
        var newContent = answer.replace(Regex("</?pre>"), "")
        newContent = newContent.replace(Regex("&lt;?"), "<")
        newContent = newContent.replace(Regex("&gt;?"), ">")
        return newContent
    }

    fun formatHTMLToAndroid(text: String): String {
        var newContent = text.replace("\n", "<br>")
        newContent = newContent.replace("<pre>", "<code>")
        newContent = newContent.replace("</pre>", "</code>")
        return newContent
    }
}