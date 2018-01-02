package com.twopixeled.zendreviewerfree.util

import android.content.Context
import android.content.SharedPreferences

class AppPreferenceUtil(ctx: Context) {
    private val context = ctx
    private val appPrefKey = "zend_reviewer_app_pref_key"
    private val questionCntPref = "question_count_pref"
    private val sharedPref: SharedPreferences

    init {
        sharedPref = context.getSharedPreferences("${context.packageName}.$appPrefKey", Context.MODE_PRIVATE)
    }

    /**
     * Acquire the maximum allowed questions for the exam
     */
    fun getQuestionsLimit(questionQty: Int): Int {
        val unlockedQuestionsCnt = sharedPref.getInt(questionCntPref, 300)

        return if (questionQty > unlockedQuestionsCnt) {
            unlockedQuestionsCnt
        } else {
            questionQty
        }
    }

    /**
     * Unlocks 15 more questions from the question bank
     */
    fun addMoreQuestions() {
        val currentLimit = getQuestionsLimit(9999)
        sharedPref.edit().putInt(questionCntPref, currentLimit + 15).apply()
    }
}