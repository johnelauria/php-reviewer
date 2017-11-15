package com.johngeli.zendreviewer.database

import android.content.Context
import com.johngeli.zendreviewer.data.QuestionsData

const val QUESTION_TYPES_TABLE = "question_types"

/**
 * Database for Question Types
 */
class Questions(context: Context) : PhpReviewDb(context) {
    private val questionTypeKey = "question_type"

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
     */
    fun getQuestions(questionType: String, questionNum: String): MutableMap<Int, QuestionsData> {
        open()
        val result = mutableMapOf<Int, QuestionsData>()
        val query = "SELECT q._id, q.question, qt.question_type, at.answer_type, a.answer, a.is_correct FROM questions q INNER JOIN question_types qt ON q.question_type_id = qt._id INNER JOIN answer_types at ON q.answer_type_id = at._id INNER JOIN answers a ON q._id = a.question_id WHERE qt.question_type = ? ORDER BY RANDOM() LIMIT ?"
        val cursor = database.rawQuery(query, listOf(questionType, questionNum).toTypedArray())
        cursor.moveToFirst()

        while (!cursor.isAfterLast) {
            val questionId = cursor.getInt(cursor.getColumnIndex("_id"))
            val answerOption = cursor.getString(cursor.getColumnIndex("answer"))

            if (!result.containsKey(questionId)) {
                result[questionId] = QuestionsData(
                        cursor.getInt(cursor.getColumnIndex("_id")),
                        cursor.getString(cursor.getColumnIndex("question")),
                        cursor.getString(cursor.getColumnIndex("question_type")),
                        cursor.getString(cursor.getColumnIndex("answer_type"))
                )
            }

            if (cursor.getInt(cursor.getColumnIndex("is_correct")) == 1) {
                result[questionId]?.correctAnswers?.add(answerOption)
            }

            result[questionId]?.answerOptions?.add(answerOption)
            cursor.moveToNext()
        }

        cursor.close()
        return result
    }

    /**
     * Generates the data for single select type questions
     */
    private fun genSingleQuestions(): MutableList<String> {
        return mutableListOf("")
    }
}