package com.johngeli.zendreviewer.data

data class QuestionsData(
        val questionId: Int,
        val question: String,
        val questionType: String,
        val correctAnswers: MutableList<String> = mutableListOf(),
        val answerOptions: MutableList<String> = mutableListOf(),
        val usersAnswers: MutableList<String> = mutableListOf(),
        val isCorrect: Boolean = false
) {
    fun trimmedQuestion(): String {
        if (question.length > 35) {
            return question.substring(0, 35)
        }

        return question
    }

    /**
     * Determine if the given answer is already selected by the user
     */
    fun isAnswerSelected(answer: String): Boolean {
        return usersAnswers.contains(answer)
    }
}