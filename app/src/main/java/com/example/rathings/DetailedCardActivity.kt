package com.example.rathings

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import java.util.*

class DetailedCardActivity : AppCompatActivity(), Observer {

    var interestCardsObs = FirebaseUtils.interestCardsObservable
    var tabsObs = FirebaseUtils.tabsObservable
    var selectedCard: Card = Card()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed_card)
        interestCardsObs.addObserver(this)
        tabsObs.addObserver(this)
        initData()
    }

    override fun update(observableObj: Observable?, data: Any?) {
        when(observableObj) {
            interestCardsObs -> {
                initData()
            }
            tabsObs -> {
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
            (findViewById(R.id.date) as TextView).text = java.text.SimpleDateFormat("yyyy-MM-dd' - 'HH:mm:ss", Locale.ITALY).format(date)

            // Profile Image
            Log.e("[DETAILED-CARD]", selectedCard.userObj.profile_image)
            if(profile_image != null && selectedCard.userObj.profile_image != "") {
                Picasso.with(this).load(selectedCard.userObj.profile_image).into(profile_image)
            }

            // Categories
            var containerCategories = findViewById(R.id.container_categories) as TableRow
            containerCategories.removeAllViews()
            var tabs = tabsObs.getValue() as ArrayList<Tab>
            for (i in 0 until selectedCard.category.size) {
                for (j in 0 until tabs.size) {
                    if (selectedCard.category[i] == tabs[j].id.toInt()) {
                        var textView = TextView(this)
                        textView.text = tabs[j].value
                        containerCategories.addView(textView)
                    }
                }
            }

            // Title
            (findViewById(R.id.title) as TextView).text = selectedCard.title

            // Description
            (findViewById(R.id.description) as TextView).text = selectedCard.description


            // Comments
            for (i in 0 until selectedCard.comments.size) {
                Log.d("[DETAILED-CARD]", selectedCard.comments[i].text)
            }

            Log.d("[DETAILED-CARD]", "Card: " + cards[cardPosition.toInt()]?.toString())
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        interestCardsObs.deleteObserver(this)
        tabsObs.deleteObserver(this)
    }

}
