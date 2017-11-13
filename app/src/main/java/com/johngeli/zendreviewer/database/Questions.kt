package com.johngeli.zendreviewer.database

import android.content.Context

const val QUESTION_TYPES_TABLE = "question_types"
const val QUESTION_TABLE = "questions"

/**
 * Database for Question Types
 */
class Questions(context: Context) : PhpReviewDb(context) {
    private val questionTypeKey = "question_type"
    private val questionKey = "question"

    /**
     * Query the list of question types from database
     */
    fun getQuestionTypes(): MutableList<String> {
        open()
        val result = mutableListOf<String>()
        val cursor = database.query(
                QUESTION_TYPES_TABLE,
                listOf(questionTypeKey).toTypedArray(),
                null, null, null, null, null
        )

        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            result.add(cursor.getString(cursor.getColumnIndex(questionTypeKey)))
            cursor.moveToNext()
        }

        cursor.close()
        return result
    }

    /**
     * Query the list of questions from database
     * TODO: Use questionType
     */
    fun getQuestions(questionType: String, questionNum: String): MutableList<String> {
        open()
        val result = mutableListOf<String>()
        val cursor = database.query(
                false,
                QUESTION_TABLE,
                listOf(questionKey).toTypedArray(),
                null, null, null, null, "RANDOM()", questionNum
        )
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            result.add(cursor.getString(cursor.getColumnIndex(questionKey)))
            cursor.moveToNext()
        }

        cursor.close()
        return result
    }
}