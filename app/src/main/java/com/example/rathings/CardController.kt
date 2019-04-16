package com.example.rathings

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import java.util.*
import kotlin.collections.ArrayList

object CardController: Observer {
    /**FIREBASE UTILS OBSERVABLE VARIABLES*/
    var interestCardsObservable=FirebaseUtils.interestCardsObservable

    /**FIREBASE UTILS OBSERVABLE VARIABLES*/
    var interestCardObs:CustomObservable=CustomObservable()

    init {
        interestCardsObservable.addObserver(this)
    }

    fun getCards(uid:String){
        FirebaseUtils.getProfile(uid)
    }

    fun interestCards(cards: ArrayList<Card>){
        var interestCards=ArrayList<Card>()
        if(FirebaseUtils.getLocalUser()!=null) {
            val interests= FirebaseUtils.getLocalUser()!!.interests

            interestCards=ArrayList(cards.filter{
                Log.d("[DEBUG]", "interests $interests")
                Log.d("[DEBUG]", "category ${it.category}")
                var count:Double = 0.0
                for (cat in it.category){
                    if (cat in interests){
                        count=count+1
                        Log.d("[DEBUG]", "count $count")
                    }
                }
                it.likelihood= count/( interests.size + it.category.size)
                Log.d("[DEBUG]", "likelihood ${it.likelihood}")
                it.likelihood>0
            })
            interestCardObs.setValue(interestCards)
            interestCards= ArrayList(interestCards.sortedWith(compareByDescending({ it.likelihood})))
        }
    }


    override fun update(observableObj: Observable?, data: Any?) {
        when(observableObj) {
            interestCardsObservable -> {
                val value = interestCardsObservable.getValue()
                if (value is List<*>) {
                    val cards: ArrayList<Card> = ArrayList(value.filterIsInstance<Card>())

                    interestCards(cards)
                    Log.d("[USER-CONTROLLER]", "observable " + cards?.toString())
                }
            }
            else -> Log.d("[USER-CONTROLLER]", "observable not recognized $data")
        }
    }
}