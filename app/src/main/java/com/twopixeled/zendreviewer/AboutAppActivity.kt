package com.twopixeled.zendreviewer

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.method.LinkMovementMethod
import kotlinx.android.synthetic.main.activity_about_app.dev1TV
import kotlinx.android.synthetic.main.activity_about_app.dev2TV

class AboutAppActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_app)

        dev1TV.movementMethod = LinkMovementMethod.getInstance()
        dev2TV.movementMethod = LinkMovementMethod.getInstance()
    }
}
