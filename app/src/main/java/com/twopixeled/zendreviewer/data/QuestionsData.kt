package com.twopixeled.zendreviewer.data

import java.util.*

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
    private var randomisedAns = mutableListOf<String>()

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
        if (answerType == "TXT") {
            usersAnswers.clear()
        } else {
            usersAnswers.remove(answer)
        }
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
     * Determine if this question has already been answered by the user
     */
    fun isAnswered(): Boolean {
        return usersAnswers.isNotEmpty()
    }

    /**
     * Acquires the list of answer options in a randomised order, but ensuring that all correct answers
     * are included
     */
    fun randomisedAnswers(): List<String> {
        if (randomisedAns.count() > 0) {
            return randomisedAns
        }

        if (correctAnswers.count() >= 5) {
            Collections.shuffle(answerOptions)
            return answerOptions
        }

        var incorrectAns = mutableListOf<String>()

        for (answer in answerOptions) {
            if (correctAnswers.contains(answer)) {
                randomisedAns.add(answer)
            } else {
                incorrectAns.add(answer)
            }
        }

        Collections.shuffle(incorrectAns)

        if (answerOptions.count() > 5) {
            incorrectAns =  incorrectAns.slice(0..(4 - randomisedAns.count())).toMutableList()
        }

        randomisedAns.addAll(incorrectAns)
        Collections.shuffle(randomisedAns)
        return randomisedAns
    }

    /**
     * Compares the user's answer vs the correct answer, then update the "isCorrect" property
     */
    private fun checkAnswer() {
        isCorrect = when {
            answerType == "TXT" -> correctAnswers.first().equals(usersAnswers.first(), true)
            correctAnswers.count() == usersAnswers.count() -> correctAnswers.containsAll(usersAnswers)
            else -> false
        }
    }
}