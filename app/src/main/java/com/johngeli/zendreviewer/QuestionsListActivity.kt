package com.johngeli.zendreviewer

import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import com.johngeli.zendreviewer.data.QuestionsData
import com.johngeli.zendreviewer.database.Questions
import kotlinx.android.synthetic.main.activity_questions_list.*
import kotlinx.android.synthetic.main.app_bar_questions_list.*

class QuestionsListActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var questionTV: TextView
    private lateinit var answersTV: TextView
    private lateinit var questionsList: MutableMap<Int, QuestionsData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questions_list)
        setSupportActionBar(toolbar)

        questionTV = findViewById(R.id.questionTV)
        answersTV = findViewById(R.id.answersTV)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
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
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        var ansOptions = ""
        Toast.makeText(this, item.itemId.toString(), Toast.LENGTH_SHORT).show()
        questionTV.text = formatHTMLToAndroid(questionsList[item.itemId]!!.question)

        for (answer in questionsList[item.itemId]!!.answerOptions) {
            ansOptions += "$answer\n"
        }
        answersTV.text = ansOptions

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun populateNavView(navView: NavigationView, questionNum: String, questionType: String) {
        val questionsDB = Questions(this)
        var ctr = 0
        var firstQuestionID: Int? = null
        questionsList = questionsDB.getQuestions(questionType, questionNum)

        for (question in questionsList) {
            firstQuestionID = firstQuestionID ?: question.key
            navView.menu.add(R.id.questionItemGroup, question.key, Menu.NONE, question.value.trimmedQuestion())
        }

        navView.menu.setGroupCheckable(R.id.questionItemGroup, true, true)
        navView.menu.getItem(0).isChecked = true
        questionTV.text = formatHTMLToAndroid(questionsList[firstQuestionID]!!.question)
    }

    private fun formatHTMLToAndroid(content: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(content, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL).toString()
        } else {
            Html.fromHtml(content).toString()
        }
    }
}
