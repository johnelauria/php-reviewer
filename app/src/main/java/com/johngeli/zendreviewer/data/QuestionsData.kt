package com.johngeli.zendreviewer.data

data class QuestionsData(
        val questionId: Int,
        val question: String,
        val questionType: String,
        val correctAnswers: MutableList<String> = mutableListOf(),
        val answerOptions: MutableList<String> = mutableListOf()
) {
    fun trimmedQuestion(): String {
        if (question.length > 35) {
            return question.substring(0, 35)
        }

        return question
    }
}