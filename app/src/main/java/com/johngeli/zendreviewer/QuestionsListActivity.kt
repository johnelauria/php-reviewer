package com.johngeli.zendreviewer

import android.graphics.drawable.Drawable
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
import android.widget.TextView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.EditText
import android.widget.CheckBox
import com.johngeli.zendreviewer.data.QuestionsData
import com.johngeli.zendreviewer.database.Questions
import kotlinx.android.synthetic.main.activity_questions_list.drawer_layout
import kotlinx.android.synthetic.main.activity_questions_list.nav_view
import kotlinx.android.synthetic.main.app_bar_questions_list.toolbar
import kotlinx.android.synthetic.main.app_bar_questions_list.nextFab
import kotlinx.android.synthetic.main.app_bar_questions_list.prevFab
import kotlinx.android.synthetic.main.content_questions_list.correctAnswerTV
import kotlinx.android.synthetic.main.content_questions_list.answersRadioGrp
import kotlinx.android.synthetic.main.content_questions_list.answersChkBxGrp
import kotlinx.android.synthetic.main.nav_header_questions_list.navHeaderText
import kotlinx.android.synthetic.main.nav_header_questions_list.navBodyText

class QuestionsListActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
        RadioGroup.OnCheckedChangeListener, CompoundButton.OnCheckedChangeListener {
    private lateinit var questionTV: TextView
    private lateinit var questionsList: MutableMap<Int, QuestionsData>
    private lateinit var answerET: EditText
    private var selectedQuestion: QuestionsData? = null
    private var isSubmitted = false
    private var questionsIndex = mutableListOf<Int>()
    private var answeredQuestionCnt = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questions_list)
        setSupportActionBar(toolbar)

        questionTV = findViewById(R.id.questionTV)
        answerET = findViewById(R.id.answerET)
        answersRadioGrp.setOnCheckedChangeListener(this)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
        nextFab.setOnClickListener { view -> nextQuestion() }
        prevFab.setOnClickListener { view -> prevQuestion() }
        populateNavView(nav_view, intent.extras.getString("questionNum"), intent.extras.getString("questionType"))
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
            R.id.submit_quiz -> displayResults()
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCheckedChanged(radio: RadioGroup?, id: Int) {
        val selectedRadio = findViewById<RadioButton>(id)
        if (selectedRadio != null) {
            setAnswer(selectedRadio.text.toString(), true)
        }
    }

    override fun onCheckedChanged(checkbox: CompoundButton?, isChecked: Boolean) {
        if (isChecked) {
            setAnswer(checkbox?.text.toString(), true)
        } else {
            setAnswer(checkbox?.text.toString(), false)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        displayQuestion(item.itemId)

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * Move to the next question
     */
    private fun nextQuestion() {
        val nextQuestionIndex = questionsIndex.indexOf(selectedQuestion!!.questionId) + 1
        val nextQuestionID = questionsList[questionsIndex[nextQuestionIndex]]!!.questionId

        nav_view.menu.findItem(nextQuestionID).isChecked = true
        displayQuestion(nextQuestionID)
    }

    /**
     * Move to the previous question
     */
    private fun prevQuestion() {
        val prevQuestionIndex = questionsIndex.indexOf(selectedQuestion!!.questionId) - 1
        val prevQuestionID = questionsList[questionsIndex[prevQuestionIndex]]!!.questionId

        nav_view.menu.findItem(prevQuestionID).isChecked = true
        displayQuestion(prevQuestionID)
    }

    /**
     * Sets the next and previous buttons visible or not depending if there is still an available
     * next or previous question
     */
    private fun toggleNextPrevBtn(questionId: Int) {
        val hasNextQuestion = questionsIndex.indexOf(questionId) < questionsIndex.size - 1
        val hasPrevQuestion = questionsIndex.indexOf(questionId) > 0

        nextFab.visibility = if (hasNextQuestion) View.VISIBLE else View.GONE
        prevFab.visibility = if (hasPrevQuestion) View.VISIBLE else View.GONE
    }

    /**
     * Displays the questions and the answer options (radio buttons, checkboxes or input text)
     */
    private fun displayQuestion(questionId: Int) {
        val questionData = questionsList[questionId]!!
        // Setup question and clear all answer options / EditText
        questionTV.text = formatHTMLToAndroid(questionData.question)
        answersRadioGrp.removeAllViews()
        answersChkBxGrp.removeAllViews()
        answerET.visibility = View.GONE

        // If previous question had single text type, save the answer from EditText first.
        if (selectedQuestion?.answerType == "TXT") {
            val answer = answerET.text.toString()
            setAnswer(answer, answer.isNotEmpty())
        }
        selectedQuestion = questionData

        for (answer in questionData.answerOptions) {
            var viewElement: View? = null
            when (questionData.answerType) {
                "SS" -> {
                    viewElement = RadioButton(this)
                    viewElement.text = answer
                    viewElement.layoutParams = RadioGroup.LayoutParams(
                            RadioGroup.LayoutParams.MATCH_PARENT,
                            RadioGroup.LayoutParams.WRAP_CONTENT
                    )
                    answersRadioGrp.addView(viewElement)
                    viewElement.isChecked = questionData.isAnswerSelected(answer)
                    viewElement.isEnabled = !isSubmitted
                }
                "MS" -> {
                    viewElement = CheckBox(this)
                    viewElement.text = answer
                    viewElement.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    answersChkBxGrp.addView(viewElement)
                    viewElement.isChecked = questionData.isAnswerSelected(answer)
                    viewElement.isEnabled = !isSubmitted
                    viewElement.setOnCheckedChangeListener(this)
                }
                "TXT" -> {
                    answerET.visibility = View.VISIBLE
                    answerET.setText(selectedQuestion?.getUserSingleAnswer())
                    answerET.isEnabled = !isSubmitted
                    viewElement = answerET
                }
            }

            markCorrectAnswers(viewElement, answer)
        }

        answersRadioGrp.visibility = View.VISIBLE
        answersChkBxGrp.visibility = View.VISIBLE
        toggleNextPrevBtn(selectedQuestion!!.questionId)
    }

    /**
     * Populate the drawer navbar with list of questions
     */
    private fun populateNavView(navView: NavigationView, questionNum: String, questionType: String) {
        val questionsDB = Questions(this)
        var firstQuestionID: Int? = null
        questionsList = questionsDB.getQuestions(questionType, questionNum)

        for (question in questionsList) {
            firstQuestionID = firstQuestionID ?: question.key
            navView.menu.add(
                    R.id.questionItemGroup,
                    question.key,
                    Menu.NONE,
                    question.value.trimmedQuestion()
            )
            questionsIndex.add(question.key)
        }

        navView.menu.setGroupCheckable(R.id.questionItemGroup, true, true)
        navView.menu.getItem(0).isChecked = true
        displayQuestion(firstQuestionID!!)
    }

    @Suppress("DEPRECATION")
    /**
     * This will format the text from the db to a more presentable formatted TextView which
     * will be rendered in the mobile device
     */
    private fun formatHTMLToAndroid(content: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(content, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL).toString()
        } else {
            Html.fromHtml(content).toString()
        }
    }

    /**
     * Calculate the total score then have it displayed. Also disables the answer options
     */
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
        markCorrectNavs()
        return true
    }

    /**
     * Highlights the correct answers in green for radios and checkboxes. If EditText is shown,
     * then show a text below it with the correct answer
     */
    private fun markCorrectAnswers(view: View?, answer: String) {
        if (isSubmitted) {
            if (view is EditText) {
                correctAnswerTV.text = resources.getString(R.string.correctAnswer, answer)
                correctAnswerTV.visibility = View.VISIBLE
            } else if (selectedQuestion!!.correctAnswers.contains(answer)) {
                view?.setBackgroundColor(resources.getColor(R.color.correctAnsMarker))
                correctAnswerTV.visibility = View.GONE
            }
        }
    }

    /**
     * This is triggered when user selects / removes an answer. This then updates the icon in
     * nav_view drawer bar, and the text on how many questions have been answered
     *
     * @param answer The answer selected/typed by user
     * @param isAdded If true, the user has selected this answer. Otherwise, the answer is removed
     * (emptied the text box or unchecked a checkbox)
     */
    private fun setAnswer(answer: String, isAdded: Boolean) {
        if (!isSubmitted) {
            val questionIndex = questionsIndex.indexOf(selectedQuestion!!.questionId)
            var icon: Drawable? = null

            if (isAdded) {
                if (!selectedQuestion!!.isAnswered()) answeredQuestionCnt++
                selectedQuestion!!.addUserAnswer(answer)
                icon = resources.getDrawable(R.drawable.btn_radio_off_holo)
            } else {
                if (selectedQuestion!!.isAnswered()) answeredQuestionCnt--
                selectedQuestion!!.removeUserAnswer(answer)
            }

            nav_view.menu.getItem(questionIndex).icon = icon
            navBodyText.text = resources.getString(
                    R.string.answerCounter,
                    answeredQuestionCnt,
                    questionsList.count()
            )
        }
    }

    /**
     * Marks the nav_view with check icon if the answer is correct. Cross icon otherwise
     */
    private fun markCorrectNavs() {
        for (question in questionsIndex.withIndex()) {
            nav_view.menu.getItem(question.index).icon = if (questionsList[question.value]!!.isCorrect) {
                resources.getDrawable(R.drawable.btn_check_buttonless_on)
            } else {
                resources.getDrawable(R.drawable.ic_menu_close_clear_cancel)
            }
        }
    }
}