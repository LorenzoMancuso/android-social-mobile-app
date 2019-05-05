package com.example.rathings

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
        var idCard: String = intent.getStringExtra("idCard")
        Log.e("[DETAILED-CARD]", idCard)
        val value = interestCardsObs.getValue()
        if (value is List<*>) {
            val cards: ArrayList<Card> = ArrayList(value.filterIsInstance<Card>())
            selectedCard = cards[idCard.toInt()]
            Log.d("[DETAILED-CARD]", "observable " + cards[idCard.toInt()]?.toString())
        }
    }
}
