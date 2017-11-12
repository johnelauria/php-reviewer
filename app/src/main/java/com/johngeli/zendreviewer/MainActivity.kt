package com.johngeli.zendreviewer

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import com.johngeli.zendreviewer.database.Questions

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), TextWatcher {
    private lateinit var questionNumET: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        startQuizFab.setOnClickListener { view ->
            startActivity(Intent("android.intent.action.QUESTIONS_LIST"))
        }
        questionNumET = findViewById<EditText>(R.id.questionNumET)
        questionNumET.addTextChangedListener(this)

        populateCategories()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
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

    override fun afterTextChanged(p0: Editable?) {

    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        val questionNum = questionNumET.text.toString()
        if (questionNum.isNotEmpty() && Integer.parseInt(questionNum) > 0) {
            startQuizFab.visibility = View.VISIBLE
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
