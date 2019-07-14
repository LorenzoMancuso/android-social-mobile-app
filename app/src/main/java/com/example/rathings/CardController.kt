package com.example.rathings

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import java.util.*
import kotlin.collections.ArrayList

object CardController: Observer {

    var interestCardsObservable=FirebaseUtils.interestCardsObservable
    var interestCardObs:CustomObservable=CustomObservable()

    init {
        interestCardsObservable.addObserver(this)
    }

    fun getUserCards(uid:String){
        return FirebaseUtils.getUserCards(uid)
    }

    fun interestCards(cards: ArrayList<Card>){
        var interestCards: ArrayList<Card>
        if(FirebaseUtils.getLocalUser()!=null) {
            val interests = FirebaseUtils.getLocalUser()!!.interests

            interestCards = ArrayList(cards.filter{
                var count = 0.0
                for (cat in it.category){
                    if (cat in interests){
                        count++
                        Log.d("[DEBUG]", "count $count")
                    }
                }
                it.likelihood= count/( interests.size + it.category.size)
                Log.d("[DEBUG]", "likelihood ${it.likelihood}")
                it.likelihood>0
            })

            interestCardObs.setValue(interestCards)
            interestCards = ArrayList(interestCards.sortedWith(compareByDescending({ it.likelihood })))
        }
    }

    override fun update(observableObj: Observable?, data: Any?) {
        when(observableObj) {
            interestCardsObservable -> {
                val value = interestCardsObservable.getValue()
                if (value is List<*>) {
                    val cards: ArrayList<Card> = ArrayList(value.filterIsInstance<Card>())
                    interestCards(cards)
                    Log.d("[CARD-CONTROLLER]", "Observable " + cards?.toString())
                }
            }
            else -> Log.d("[CARD-CONTROLLER]", "Observable not recognized $data")
        }
    }
}