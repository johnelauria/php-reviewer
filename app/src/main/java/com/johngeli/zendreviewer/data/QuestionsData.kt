package com.johngeli.zendreviewer.data

data class QuestionsData(
        val questionId: Int,
        val question: String,
        val questionType: String,
        val answerType: String,
        val correctAnswers: MutableList<String> = mutableListOf(),
        val answerOptions: MutableList<String> = mutableListOf(),
        private val usersAnswers: MutableList<String> = mutableListOf(),
        var isCorrect: Boolean = false
) {
    fun trimmedQuestion(): String {
        if (question.length > 35) {
            return question.substring(0, 35)
        }

        return question
    }

    /**
     * Remember the user's selected answer for this current question. If question type is not
     * multiple select, then ensure that only 1 answer can be selected for this question
     */
    fun addUserAnswer(answer: String) {
        if (answerType != "MS") {
            usersAnswers.clear()
        }
        usersAnswers.add(answer)
        checkAnswer()
    }

    /**
     * Remove the user's answer. This should happen when a user un-checks the answer from a
     * Multiple Select question
     */
    fun removeUserAnswer(answer: String) {
        usersAnswers.remove(answer)
    }

    /**
     * If answer type is Text type, then there can only be 1 answer selected by the user. That
     * single answer is returned
     */
    fun getUserSingleAnswer(): String? {
        if (usersAnswers.isNotEmpty()) {
            return usersAnswers.first()
        }

        return null
    }

    /**
     * Determine if the given answer is already selected by the user
     */
    fun isAnswerSelected(answer: String): Boolean {
        return usersAnswers.contains(answer)
    }

    /**
     * Compares the user's answer vs the correct answer, then update the "isCorrect" property
     */
    private fun checkAnswer() {
        isCorrect = if (correctAnswers.count() == usersAnswers.count()) {
            correctAnswers.containsAll(usersAnswers)
        } else {
            false
        }
    }
}