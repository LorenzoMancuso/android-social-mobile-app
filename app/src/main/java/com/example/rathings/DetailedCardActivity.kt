package com.example.rathings

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import java.util.*

class DetailedCardActivity : AppCompatActivity(), Observer {

    var interestCardsObs = FirebaseUtils.interestCardsObservable
    var selectedCard: Card = Card()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed_card)
        interestCardsObs.addObserver(this)
        initData()
    }

    override fun update(observableObj: Observable?, data: Any?) {
        when(observableObj) {
            interestCardsObs -> {
                initData()
            }
            else -> Log.d("[DETAILED-CARD]", "observable not recognized $data")
        }
    }

    private fun initData() {
        var cardPosition: String = intent.getStringExtra("card_position")
        Log.e("[DETAILED-CARD]", cardPosition)
        val value = interestCardsObs.getValue()
        if (value is List<*>) {
            val cards: ArrayList<Card> = ArrayList(value.filterIsInstance<Card>())
            selectedCard = cards[cardPosition.toInt()]

            // User Name
            (findViewById(R.id.user) as TextView).text = "${selectedCard.userObj.name} ${selectedCard.userObj.surname}"

            // Date
            val date = java.util.Date(selectedCard.timestamp.toLong() * 1000)
            (findViewById(R.id.date) as TextView).text = java.text.SimpleDateFormat("yyyy-MM-dd' - 'HH:mm:ss").format(date)

            // Profile Image
            Log.e("[DETAILED-CARD]", selectedCard.userObj.profile_image)
            if(profile_image != null && selectedCard.userObj.profile_image != "") {
                Picasso.with(this).load(selectedCard.userObj.profile_image).into(profile_image)
            }

            // Categories
            var containerCategories = findViewById(R.id.container_categories) as TableRow
            containerCategories.removeAllViews()
            for (i in 0 until selectedCard.category.size) {
                var textView = TextView(this)
                textView.text = selectedCard.category[i].toString()
                containerCategories.addView(textView)
            }

            // Title
            (findViewById(R.id.title) as TextView).text = selectedCard.title

            // Description
            (findViewById(R.id.description) as TextView).text = selectedCard.description

            Log.d("[DETAILED-CARD]", "Card: " + cards[cardPosition.toInt()]?.toString())
        }
    }
}
