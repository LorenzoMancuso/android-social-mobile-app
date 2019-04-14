package com.example.rathings

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toolbar

class NewCardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_card)

        /*val mToolbar = findViewById(R.id.toolbar) as Toolbar
        mToolbar.setTitle(getString(R.string.app_name))
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)

        mToolbar.setNavigationOnClickListener(object : View.OnClickListener() {
            fun onClick(view: View) {
                finish()
            }
        })*/
    }

    fun publishCard(view: View) {
        val title = findViewById(R.id.title_text) as EditText
        val description = findViewById(R.id.desc_text) as EditText
        println(title.text)
        println(description.text)
        finish()
    }
}
