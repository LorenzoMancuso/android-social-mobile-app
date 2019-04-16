package com.example.rathings

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import java.util.*

object CardController: Observer {

    var interestCardsObservable=FirebaseUtils.userProfileObservable

    init {
        //localUserProfileObservable.addObserver(this)
    }

    fun getCards(uid:String){
        FirebaseUtils.getProfile(uid)
    }

    fun interestCards(cards: ArrayList<Card>){
        var interestCards=ArrayList<Card>()
        if(FirebaseUtils.getLocalUser()!=null) {
            val interests= FirebaseUtils.getLocalUser()!!.interests

            interestCards=ArrayList(cards.filter{
                it.category[0]==0
            })
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