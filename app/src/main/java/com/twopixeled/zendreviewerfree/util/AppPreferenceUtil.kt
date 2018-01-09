package com.twopixeled.zendreviewerfree.util

import android.content.Context
import android.content.SharedPreferences

class AppPreferenceUtil(ctx: Context) {
    private val context = ctx
    private val appPrefKey = "zend_reviewer_app_pref_key"
    private val questionCntPref = "question_count_pref"
    private val dbVersionPref = "db_version_pref"
    private val sharedPref: SharedPreferences

    init {
        sharedPref = context.getSharedPreferences("${context.packageName}.$appPrefKey", Context.MODE_PRIVATE)
    }

    /**
     * Acquire the maximum allowed questions for the exam
     */
    fun getQuestionsLimit(): Int {
        return sharedPref.getInt(questionCntPref, 300)
    }

    /**
     * Unlocks 15 more questions from the question bank
     */
    fun addMoreQuestions() {
        val currentLimit = getQuestionsLimit()
        sharedPref.edit().putInt(questionCntPref, currentLimit + 15).apply()
    }

    /**
     * Get the current database version of the app. This is used to determine if the database
     * should be updated or not.
     */
    fun getDatabaseVersion(): Int {
        return sharedPref.getInt(dbVersionPref, 0)
    }

    /**
     * Sets the current database version
     */
    fun setDatabaseVersion(version: Int) {
        sharedPref.edit().putInt(dbVersionPref, version).apply()
    }
}