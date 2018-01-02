package com.twopixeled.zendreviewerfree.database

import android.content.Context
import com.twopixeled.zendreviewerfree.data.QuestionsData
import com.twopixeled.zendreviewerfree.util.AppPreferenceUtil
import com.twopixeled.zendreviewerfree.util.TextUtil

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
        val result = mutableListOf("All")
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
     * Query the list of questions from the database in QuestionsData object, which will contain
     * question, answers, correct answers and other data
     */
    fun getQuestions(questionType: String, questionNum: String): MutableMap<Int, QuestionsData> {
        open()
        val result = mutableMapOf<Int, QuestionsData>()
        val bindParams = mutableListOf<String>()
        val questionsQuery = StringBuilder("SELECT q._id, q.question, q.answer_type_id, qt.question_type FROM questions q")
        questionsQuery.append(" INNER JOIN question_types qt ON q.question_type_id = qt._id")

        if ("All" != questionType) {
            questionsQuery.append(" WHERE qt.question_type = ?")
            bindParams.add(questionType)
        }

        questionsQuery.append(" AND q._id IN (SELECT _id FROM questions ORDER BY RANDOM()) LIMIT ?")
        bindParams.add(AppPreferenceUtil(context).getQuestionsLimit(questionNum.toInt()).toString())

        val questionsCursor = database.rawQuery(questionsQuery.toString(), bindParams.toTypedArray())
        questionsCursor.moveToFirst()
        val questionIds = mutableListOf<Int>()

        while (!questionsCursor.isAfterLast) {
            val questionId = questionsCursor.getInt(questionsCursor.getColumnIndex("_id"))

            if (!result.containsKey(questionId)) {
                questionIds.add(questionId)
                result[questionId] = QuestionsData(
                        questionsCursor.getInt(questionsCursor.getColumnIndex("_id")),
                        questionsCursor.getString(questionsCursor.getColumnIndex("question")),
                        questionsCursor.getString(questionsCursor.getColumnIndex("question_type")),
                        questionsCursor.getString(questionsCursor.getColumnIndex("answer_type_id"))
                )
            }

            questionsCursor.moveToNext()
        }

        attachAnswersData(questionIds, result)
        questionsCursor.close()
        return result
    }

    private fun attachAnswersData(questionIds: MutableList<Int>, result: MutableMap<Int, QuestionsData>) {
        val textUtil = TextUtil()
        val answersCursor = database.rawQuery(
                "SELECT * FROM answers WHERE question_id IN (${questionIds.joinToString()})",
                null
        )
        answersCursor.moveToFirst()

        while (!answersCursor.isAfterLast) {
            val questionId = answersCursor.getInt(answersCursor.getColumnIndex("question_id"))
            val answer = answersCursor.getString(answersCursor.getColumnIndex("answer"))

            if (answersCursor.getInt(answersCursor.getColumnIndex("is_correct")) == 1) {
                result[questionId]?.correctAnswers?.add(textUtil.formatAnswerOpts(answer))
            }

            result[questionId]?.answerOptions?.add(textUtil.formatAnswerOpts(answer))
            answersCursor.moveToNext()
        }

        answersCursor.close()
    }
}