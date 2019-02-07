package com.twopixeled.zendreviewerfree

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.twopixeled.zendreviewerfree.data.QuestionsData
import com.twopixeled.zendreviewerfree.database.Questions
import com.twopixeled.zendreviewerfree.util.AdMobUtil
import com.twopixeled.zendreviewerfree.util.TextUtil
import kotlinx.android.synthetic.main.activity_questions_list.drawer_layout
import kotlinx.android.synthetic.main.activity_questions_list.nav_view
import kotlinx.android.synthetic.main.app_bar_questions_list.toolbar
import kotlinx.android.synthetic.main.app_bar_questions_list.nextFab
import kotlinx.android.synthetic.main.app_bar_questions_list.prevFab
import kotlinx.android.synthetic.main.app_bar_questions_list.submitFab
import kotlinx.android.synthetic.main.content_questions_list.correctAnswerTV
import kotlinx.android.synthetic.main.content_questions_list.questionTV
import kotlinx.android.synthetic.main.content_questions_list.answerET
import kotlinx.android.synthetic.main.content_questions_list.answersRadioGrp
import kotlinx.android.synthetic.main.content_questions_list.answersChkBxGrp
import kotlinx.android.synthetic.main.content_questions_list.reviewAnsBtn
import kotlinx.android.synthetic.main.content_questions_list.backToMenuBtn
import kotlinx.android.synthetic.main.content_questions_list.questionsAdView
import kotlinx.android.synthetic.main.content_questions_list.resultsLayout
import kotlinx.android.synthetic.main.content_questions_list.scoreTV
import kotlinx.android.synthetic.main.content_questions_list.unlockMoreBtn

class QuestionsListActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
        RadioGroup.OnCheckedChangeListener, CompoundButton.OnCheckedChangeListener, TextWatcher {
    private lateinit var questionsList: MutableMap<Int, QuestionsData>
    private lateinit var menu: Menu
    private lateinit var mInterstitialAd: InterstitialAd
    private var selectedQuestion: QuestionsData? = null
    private var isSubmitted = false
    private var questionsIndex = mutableListOf<Int>()
    private var answeredQuestionCnt = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questions_list)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        answersRadioGrp.setOnCheckedChangeListener(this)
        nav_view.setNavigationItemSelectedListener(this)
        nextFab.setOnClickListener { _ -> nextQuestion() }
        prevFab.setOnClickListener { _ -> prevQuestion() }
        submitFab.setOnClickListener { _ -> submitQuiz() }
        reviewAnsBtn.setOnClickListener { _ -> drawer_layout.openDrawer(nav_view) }
        backToMenuBtn.setOnClickListener { _ -> onBackPressed() }
        unlockMoreBtn.setOnClickListener { _ -> startActivity(Intent("android.intent.action.UNLOCK_QUESTIONS_FREE")) }
        answerET.addTextChangedListener(this)
        populateNavView(nav_view, intent.extras.getString("questionNum"), intent.extras.getString("questionType"))
        questionsAdView.loadAd(AdMobUtil().buildAdRequest())
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = "ca-app-pub-8537542711636630/9688547757"
        mInterstitialAd.loadAd(AdMobUtil().buildAdRequest())
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            val alert = AlertDialog.Builder(this)
            alert.setMessage("Are you sure you want to leave the quiz?")
            alert.setPositiveButton("yes", { _: DialogInterface, _: Int ->
                super.onBackPressed()
            })
            alert.setNegativeButton("No", null)
            alert.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (questionsList.isNotEmpty()) {
            menuInflater.inflate(R.menu.questions_list, menu)
            this.menu = menu
            return true
        }

        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.submit_quiz -> submitQuiz()
            R.id.backToMenuMI -> {
                onBackPressed()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCheckedChanged(radioGrp: RadioGroup?, id: Int) {
        val selectedRadio = findViewById<RadioButton>(id)
        var radioCnt = 0

        while (radioCnt < radioGrp!!.childCount) {
            val radio = radioGrp.getChildAt(radioCnt++) as RadioButton
            radio.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorBackground, null))
            markCorrectAnswers(radio, radio.text.toString())
        }

        if (selectedRadio != null) {
            selectedRadio.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorPrimaryLight, null))
            setAnswer(selectedRadio.text.toString(), true)
        }
    }

    override fun onCheckedChanged(checkbox: CompoundButton?, isChecked: Boolean) {
        if (isChecked) {
            checkbox?.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorPrimaryLight, null))
            setAnswer(checkbox?.text.toString(), true)
        } else {
            checkbox?.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorBackground, null))
            setAnswer(checkbox?.text.toString(), false)
        }

        markCorrectAnswers(checkbox, checkbox?.text.toString())
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        showQuestionAndAns(item.itemId)
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun afterTextChanged(p0: Editable?) {
        val etContent = answerET.text
        setAnswer(etContent.toString(), etContent.isNotEmpty())
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    /**
     * Move to the next question
     */
    private fun nextQuestion() {
        val nextQuestionIndex = questionsIndex.indexOf(selectedQuestion!!.questionId) + 1
        val nextQuestionID = questionsList[questionsIndex[nextQuestionIndex]]!!.questionId

        nav_view.menu.findItem(nextQuestionID).isChecked = true
        showQuestionAndAns(nextQuestionID)
    }

    /**
     * Move to the previous question
     */
    private fun prevQuestion() {
        val prevQuestionIndex = questionsIndex.indexOf(selectedQuestion!!.questionId) - 1
        val prevQuestionID = questionsList[questionsIndex[prevQuestionIndex]]!!.questionId

        nav_view.menu.findItem(prevQuestionID).isChecked = true
        showQuestionAndAns(prevQuestionID)
    }

    /**
     * Sets the next and previous buttons visible or not depending if there is still an available
     * next or previous question
     */
    private fun toggleNextPrevSubmit(questionId: Int) {
        val hasNextQuestion = questionsIndex.indexOf(questionId) < questionsIndex.size - 1
        val hasPrevQuestion = questionsIndex.indexOf(questionId) > 0
        val lastQuestion = questionsList[questionsIndex.last()]

        nextFab.visibility = if (hasNextQuestion) View.VISIBLE else View.GONE
        prevFab.visibility = if (hasPrevQuestion) View.VISIBLE else View.GONE

        submitFab.visibility = if (!isSubmitted && !hasNextQuestion && lastQuestion!!.isAnswered()) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    /**
     * Displays the questions and the answer options (radio buttons, checkboxes or input text)
     */
    private fun showQuestionAndAns(questionId: Int) {
        val questionData = questionsList[questionId]!!
        val textUtil = TextUtil()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        // Setup question and clear all answer options / EditText
        questionTV.setHtml(textUtil.formatHTMLToAndroid(questionData.question))
        answersRadioGrp.removeAllViews()
        answersChkBxGrp.removeAllViews()
        answerET.visibility = View.GONE
        resultsLayout.visibility = View.GONE
        imm.hideSoftInputFromWindow(answerET.windowToken, 0)
        selectedQuestion = questionData

        for (answer in questionData.randomisedAnswers()) {
            var viewElement: View? = null
            when (questionData.answerType) {
                "SS" -> {
                    viewElement = RadioButton(this)
                    viewElement.text = answer
                    viewElement.layoutParams = RadioGroup.LayoutParams(
                            RadioGroup.LayoutParams.MATCH_PARENT,
                            RadioGroup.LayoutParams.WRAP_CONTENT
                    )
                    viewElement.setPadding(20, 0, 0, 30)
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
                    viewElement.setPadding(20, 0, 0, 30)
                    answersChkBxGrp.addView(viewElement)

                    if (questionData.isAnswerSelected(answer)) {
                        viewElement.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorPrimaryLight, null))
                    }

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
        toggleNextPrevSubmit(selectedQuestion!!.questionId)
    }

    /**
     * Populate the drawer navbar with list of questions
     */
    private fun populateNavView(navView: NavigationView, questionNum: String, questionType: String) {
        val questionsDB = Questions(this)
        var firstQuestionID: Int? = null
        questionsList = questionsDB.getQuestions(questionType, questionNum)

        if (questionsList.isNotEmpty()) {
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
            showQuestionAndAns(firstQuestionID!!)
        } else {
            questionTV.text = resources.getString(R.string.noQuestionsAvailable)
            submitFab.visibility = View.GONE
        }
    }

    /**
     * Calculate the total score then have it displayed. Also disables the answer options
     */
    private fun submitQuiz(): Boolean {
        var score = 0
        val totalQuestions = questionsList.count()
        val navHeaderText: TextView = nav_view.getHeaderView(0).findViewById(R.id.navHeaderText)
        val navBodyText: TextView = nav_view.getHeaderView(0).findViewById(R.id.navBodyText)

        for (questionData in questionsList) {
            if (questionData.value.isCorrect) score++
        }

        scoreTV.text = resources.getString(R.string.scoreFormat, score, totalQuestions)
        navHeaderText.text = resources.getString(R.string.yourScore)
        resultsLayout.visibility = View.VISIBLE
        questionTV.text = ""
        menu.findItem(R.id.submit_quiz).isVisible = false
        menu.findItem(R.id.backToMenuMI).isVisible = true

        navBodyText.text = resources.getString(R.string.scoreFormat, score, totalQuestions)
        answersRadioGrp.visibility = View.GONE
        answersChkBxGrp.visibility = View.GONE
        submitFab.visibility = View.GONE
        answerET.visibility = View.GONE
        isSubmitted = true
        markCorrectNavs()

        if (mInterstitialAd.isLoaded) {
            mInterstitialAd.show()
        }
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
                view?.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.correctAnsMarker, null))
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
            val navBodyText: TextView = nav_view.getHeaderView(0).findViewById(R.id.navBodyText)
            var icon: Drawable? = null

            if (isAdded) {
                if (!selectedQuestion!!.isAnswered()) answeredQuestionCnt++
                selectedQuestion!!.addUserAnswer(answer)
                icon = ResourcesCompat.getDrawable(resources, R.mipmap.btn_radio_off_holo, null)
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
            toggleNextPrevSubmit(selectedQuestion!!.questionId)
        }
    }

    /**
     * Marks the nav_view with check icon if the answer is correct. Cross icon otherwise
     */
    private fun markCorrectNavs() {
        for (question in questionsIndex.withIndex()) {
            nav_view.menu.getItem(question.index).icon = if (questionsList[question.value]!!.isCorrect) {
                ResourcesCompat.getDrawable(resources, R.mipmap.btn_check_buttonless_on, null)
            } else {
                ResourcesCompat.getDrawable(resources, R.mipmap.ic_menu_close_clear_cancel, null)
            }
        }
    }
}