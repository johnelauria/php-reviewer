package com.twopixeled.zendreviewerfree

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import com.google.android.gms.ads.MobileAds
import com.twopixeled.zendreviewerfree.database.Questions
import com.twopixeled.zendreviewerfree.util.AdMobUtil

import kotlinx.android.synthetic.main.activity_main.startQuizFab
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.content_main.mainAdView

class MainActivity : AppCompatActivity(), TextWatcher {
    private lateinit var questionNumET: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        MobileAds.initialize(this, "ca-app-pub-8537542711636630~3659302397")
        questionNumET = findViewById(R.id.questionNumET)
        val categorySpinner = findViewById<Spinner>(R.id.categorySpinner)

        startQuizFab.setOnClickListener { _ ->
            val questionsListIntent = Intent("android.intent.action.QUESTIONS_LIST_FREE")
            questionsListIntent.putExtra("questionNum", questionNumET.text.toString())
            questionsListIntent.putExtra("questionType", categorySpinner.selectedItem.toString())
            startActivity(questionsListIntent)
        }
        questionNumET.addTextChangedListener(this)
        populateCategories()
        mainAdView.loadAd(AdMobUtil().buildAdRequest())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_about -> {
                startActivity(Intent("android.intent.action.ABOUT_APP_FREE"))
                return true
            }
            R.id.action_unlock_questions -> {
                startActivity(Intent("android.intent.action.UNLOCK_QUESTIONS_FREE"))
                return true
            }
            R.id.action_buy_full_app -> {
                val browserIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=com.twopixeled.zendreviewer")
                )
                startActivity(browserIntent)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun afterTextChanged(p0: Editable?) {

    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        val questionNum = questionNumET.text.toString()
        if (questionNum.isNotEmpty() && Integer.parseInt(questionNum) > 0) {
            startQuizFab.visibility = View.VISIBLE
        } else {
            startQuizFab.visibility = View.GONE
        }
    }

    private fun populateCategories() {
        val categorySpinner = findViewById<Spinner>(R.id.categorySpinner)
        val question = Questions(this)
        val categoryAdapter = ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                question.getQuestionTypes()
        )
        categorySpinner.adapter = categoryAdapter
    }
}
