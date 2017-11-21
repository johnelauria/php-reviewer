package com.johngeli.zendreviewer

import android.os.Build
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.johngeli.zendreviewer.data.QuestionsData
import com.johngeli.zendreviewer.database.Questions
import kotlinx.android.synthetic.main.activity_questions_list.*
import kotlinx.android.synthetic.main.app_bar_questions_list.*
import kotlinx.android.synthetic.main.nav_header_questions_list.*

class QuestionsListActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
        RadioGroup.OnCheckedChangeListener, CompoundButton.OnCheckedChangeListener {
    private lateinit var questionTV: TextView
    private lateinit var questionsList: MutableMap<Int, QuestionsData>
    private lateinit var answersRadioGrp: RadioGroup
    private lateinit var answersChkBxGrp: LinearLayout
    private lateinit var answerET: EditText
    private var selectedQuestion: QuestionsData? = null
    private var isSubmitted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questions_list)
        setSupportActionBar(toolbar)

        questionTV = findViewById(R.id.questionTV)
        answersRadioGrp = findViewById(R.id.answersRadioGrp)
        answersChkBxGrp = findViewById(R.id.answersChkBxGrp)
        answerET = findViewById(R.id.answerET)
        answersRadioGrp.setOnCheckedChangeListener(this)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
        populateNavView(nav_view, intent.extras.getString("questionNum"), intent.extras.getString("questionType"))

        fab.setOnClickListener { view ->
                // todo iterate to next question
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.questions_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.submit_quiz-> displayResults()
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCheckedChanged(radio: RadioGroup?, id: Int) {
        val selectedRadio = findViewById<RadioButton>(id)
        if (selectedRadio != null) {
            selectedQuestion?.addUserAnswer(selectedRadio.text.toString())
        }
    }

    override fun onCheckedChanged(checkbox: CompoundButton?, isChecked: Boolean) {
        if (isChecked) {
            selectedQuestion?.addUserAnswer(checkbox?.text.toString())
        } else {
            selectedQuestion?.removeUserAnswer(checkbox?.text.toString())
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        displayQuestion(item.itemId)

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun displayQuestion(questionId: Int) {
        val questionData = questionsList[questionId]!!
        // Setup question and clear all answer options / EditText
        questionTV.text = formatHTMLToAndroid(questionData.question)
        answersRadioGrp.removeAllViews()
        answersChkBxGrp.removeAllViews()
        answerET.visibility = View.GONE

        // If previous question had single text type, save the answer from EditText first.
        if (selectedQuestion?.answerType == "TXT") {
            selectedQuestion?.addUserAnswer(answerET.text.toString())
        }
        selectedQuestion = questionData

        for (answer in questionData.answerOptions) {
            when (questionData.answerType) {
                "SS" -> {
                    val radioButton = RadioButton(this)
                    radioButton.text = answer
                    answersRadioGrp.addView(radioButton)
                    radioButton.isChecked = questionData.isAnswerSelected(answer)
                    radioButton.isEnabled = !isSubmitted
                }
                "MS" -> {
                    val checkBox = CheckBox(this)
                    checkBox.text = answer
                    answersChkBxGrp.addView(checkBox)
                    checkBox.isChecked = questionData.isAnswerSelected(answer)
                    checkBox.isEnabled = !isSubmitted
                    checkBox.setOnCheckedChangeListener(this)
                }
                "TXT" -> {
                    answerET.visibility = View.VISIBLE
                    answerET.setText(selectedQuestion?.getUserSingleAnswer())
                    answerET.isEnabled = !isSubmitted
                }
            }
        }

        answersRadioGrp.visibility = View.VISIBLE
        answersChkBxGrp.visibility = View.VISIBLE
    }

    private fun populateNavView(navView: NavigationView, questionNum: String, questionType: String) {
        val questionsDB = Questions(this)
        var firstQuestionID: Int? = null
        questionsList = questionsDB.getQuestions(questionType, questionNum)

        for (question in questionsList) {
            firstQuestionID = firstQuestionID ?: question.key
            navView.menu.add(R.id.questionItemGroup, question.key, Menu.NONE, question.value.trimmedQuestion())
        }

        navView.menu.setGroupCheckable(R.id.questionItemGroup, true, true)
        navView.menu.getItem(0).isChecked = true
        displayQuestion(firstQuestionID!!)
    }

    private fun formatHTMLToAndroid(content: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(content, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL).toString()
        } else {
            Html.fromHtml(content).toString()
        }
    }

    private fun displayResults(): Boolean {
        var score = 0
        val totalQuestions = questionsList.count()

        for (questionData in questionsList) {
            if (questionData.value.isCorrect) score++
        }

        if (score > (totalQuestions / 2)) {
            questionTV.text = resources.getString(R.string.passedResultMsg, score, totalQuestions)
            navHeaderText.text = resources.getString(R.string.passed)
        } else {
            questionTV.text = resources.getString(R.string.failedResultMsg, score, totalQuestions)
            navHeaderText.text = resources.getString(R.string.failed)
        }

        navBodyText.text = resources.getString(R.string.yourScore, score, totalQuestions)
        answersRadioGrp.visibility = View.GONE
        answersChkBxGrp.visibility = View.GONE
        answerET.visibility = View.GONE
        isSubmitted = true
        return true
    }
}
