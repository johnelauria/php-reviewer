package com.twopixeled.zendreviewerfree.util

/**
 * Utility class for formatting texts
 */
class TextUtil {
    fun formatAnswerOpts(answer: String): String {
        return answer.replace(Regex("</?pre>"), "")
    }

    fun formatHTMLToAndroid(text: String): String {
        var newContent = text.replace("\n", "<br>")
        newContent = newContent.replace("<pre>", "<code>")
        newContent = newContent.replace("</pre>", "</code>")
        return newContent
    }
}