package com.example.rathings

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import kotlinx.android.synthetic.main.activity_edit_card.*
import kotlinx.android.synthetic.main.activity_modify_account.*
import kotlinx.android.synthetic.main.activity_modify_account.confirm_button

class EditCardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_card)

        confirm_button.setOnClickListener { updateCard() }

        var selectedCard: Card = intent.getSerializableExtra("card") as Card

        txt_title.text = Editable.Factory.getInstance().newEditable("${selectedCard.title}")
        txt_description.text = Editable.Factory.getInstance().newEditable("${selectedCard.description}")

        Log.e("[EDIT-CARD]", selectedCard.toString())
    }

    fun updateCard() {

    }
}
