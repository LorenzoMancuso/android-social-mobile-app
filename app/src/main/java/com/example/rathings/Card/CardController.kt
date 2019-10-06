package com.example.rathings.Card

import android.util.Log
import com.example.rathings.utils.CustomObservable
import com.example.rathings.FirebaseUtils
import com.example.rathings.User.User
import io.github.ponnamkarthik.richlinkpreview.ResponseListener
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

        FirebaseUtils.getPrimaryProfile().addObserver( object: Observer{
            override fun update(observableObj: Observable?, data: Any?) {
                Log.d("[CARD-CONTROLLER]", "***Primary profile observable in popular cards***")
                val userObs = observableObj as CustomObservable
                val user = userObs.getValue() as User
                var popularCards: ArrayList<Card>
                val interests = user.interests

                popularCards = ArrayList(cards.filter {
                    var count = 0.0
                    for (cat in it.category) {
                        if (cat in interests) {
                            count++
                        }
                    }
                    it.likelihood = count / (interests.size + it.category.size)
                    it.likelihood > 0
                })
                popularCards = ArrayList(popularCards.sortedWith(compareByDescending({ (it.ratings_average - 3) * it.ratings_count + (it.timestamp / 86400) + it.likelihood })))
                popularCardObs.setValue(popularCards)
            }
        })
    }

    fun interestCards(cards: ArrayList<Card>){

        FirebaseUtils.getPrimaryProfile().addObserver( object: Observer{
            override fun update(observableObj: Observable?, data: Any?) {
                val userObs = observableObj as CustomObservable
                val user = userObs.getValue() as User
                var interestCards: ArrayList<Card>
                val interests = user.interests
                val followed = user.followed

                interestCards = ArrayList(cards.filter { followed.contains(it.user) })

                interestCards = ArrayList(interestCards.filter {
                    var count = 0.0
                    for (cat in it.category) {
                        if (cat in interests) {
                            count++
                        }
                    }
                    it.likelihood = count / (interests.size + it.category.size)
                    it.likelihood > 0
                })
                interestCards = ArrayList(cards.filter { it.likelihood <= 0 })

                interestCards = ArrayList(interestCards.sortedWith(compareByDescending({ (it.ratings_average - 3) * it.ratings_count + (it.timestamp / 86400) + it.likelihood })))
                interestCardObs.setValue(interestCards)
            }
        })
    }

    fun deleteCard(id: String) {
        var cards = interestCardsObservable.getValue()
        if (cards is List<*>) {
            interestCardsObservable.setValue(cards.filter { it is Card && it.id != id })
            FirebaseUtils.deleteData("cards/${id}")
        }
    }

    fun deleteMedia(idMedia: String, idCard: String) {
        var cards = interestCardsObservable.getValue() as ArrayList<Card>
        var found = false
        for (card in cards) {
            for (media in card.multimedia) {
                if (media == idMedia && card.id == idCard) {
                    card.multimedia.removeAt(card.multimedia.indexOf(media))
                    found = true
                    break
                }
            }
            if(found) {
                break
            }
        }
        FirebaseUtils.deleteData("cards/${idCard}/${idMedia}")
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