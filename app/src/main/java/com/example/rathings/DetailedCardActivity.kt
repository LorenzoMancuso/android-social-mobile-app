package com.example.rathings

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.*
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
        // TODO: Qui c'è un bug. Funziona quando sei nella home, ma se vai nel profilo, basandosi su un array diverso da interestCards non funziona.
        // 1) O distinguere se ci si trova nel PROFILO o nella HOME per ricercare in ArrayList differenti
        // 2) O passare completamente la card in input (scelta più valida, ma come???)
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
            var containerCategories = findViewById(R.id.container_categories) as LinearLayout
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
            var cardRecyclerView = findViewById(R.id.recycler_comments) as RecyclerView
            val mLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            cardRecyclerView?.layoutManager = mLayoutManager
            var commentAdapter = CommentAdapter(selectedCard.comments as ArrayList<Comment>)
            cardRecyclerView?.adapter = commentAdapter

            Log.d("[DETAILED-CARD]", "Card: " + cards[cardPosition.toInt()]?.toString())
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        interestCardsObs.deleteObserver(this)
        tabsObs.deleteObserver(this)
    }

}
