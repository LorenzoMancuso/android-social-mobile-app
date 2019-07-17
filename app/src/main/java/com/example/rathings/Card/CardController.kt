package com.example.rathings.Card

import android.util.Log
import com.example.rathings.utils.CustomObservable
import com.example.rathings.FirebaseUtils
import java.util.*
import kotlin.collections.ArrayList

object CardController: Observer {

    var interestCardsObservable= FirebaseUtils.interestCardsObservable

    var interestCardObs: CustomObservable = CustomObservable()
    var popularCardObs: CustomObservable = CustomObservable()

    init {
        interestCardsObservable.addObserver(this)
    }

    fun getUserCards(uid:String){
        return FirebaseUtils.getUserCards(uid)
    }

    fun popularCards(cards: ArrayList<Card>){
        var popularCards: ArrayList<Card>
        if(FirebaseUtils.getLocalUser() !=null) {
            val interests = FirebaseUtils.getLocalUser()!!.interests

            popularCards = ArrayList(cards.filter{
                var count = 0.0
                for (cat in it.category){
                    if (cat in interests){
                        count++
                    }
                }
                it.likelihood= count/( interests.size + it.category.size)
                it.likelihood>0
            })
            popularCards = ArrayList(popularCards.sortedWith(compareByDescending({ (it.ratings_average - 3) * it.ratings_count + (it.timestamp / 86400) + it.likelihood })))
            popularCardObs.setValue(popularCards)
        }
    }

    fun interestCards(cards: ArrayList<Card>){
        var interestCards: ArrayList<Card>
        if(FirebaseUtils.getLocalUser() !=null) {
            val interests = FirebaseUtils.getLocalUser()!!.interests
            val followed = FirebaseUtils.getLocalUser()!!.followed

            interestCards = ArrayList(cards.filter { followed.contains(it.user)})

            interestCards = ArrayList(interestCards.filter{
                var count = 0.0
                for (cat in it.category){
                    if (cat in interests){
                        count++
                    }
                }
                it.likelihood= count/( interests.size + it.category.size)
                it.likelihood>0
            })
            interestCards = ArrayList(cards.filter { it.likelihood <= 0})

            interestCards = ArrayList(interestCards.sortedWith(compareByDescending({ (it.ratings_average - 3) * it.ratings_count + (it.timestamp / 86400) + it.likelihood })))
            interestCardObs.setValue(interestCards)
        }
    }

    override fun update(observableObj: Observable?, data: Any?) {
        when(observableObj) {
            interestCardsObservable -> {
                val value = interestCardsObservable.getValue()
                if (value is List<*>) {
                    val cards: ArrayList<Card> = ArrayList(value.filterIsInstance<Card>())
                    popularCards(cards)
                    interestCards(cards)
                }
            }
            else -> Log.d("[CARD-CONTROLLER]", "Observable not recognized $data")
        }
    }
}