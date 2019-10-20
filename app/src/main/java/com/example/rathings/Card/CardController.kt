package com.example.rathings.Card

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.View
import com.example.rathings.utils.CustomObservable
import com.example.rathings.FirebaseUtils
import com.example.rathings.User.User
import io.github.ponnamkarthik.richlinkpreview.ResponseListener
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

object CardController: Observer {

    var interestCardsObservable= FirebaseUtils.interestCardsObservable

    var interestCardObs: CustomObservable = CustomObservable()
    var popularCardObs: CustomObservable = CustomObservable()

    init {
        interestCardsObservable.addObserver(this)
    }

    fun popularCards(cards: ArrayList<Card>){

        val user = FirebaseUtils.primaryUserProfileObservable.getValue() as User
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

    fun interestCards(cards: ArrayList<Card>){
        val user = FirebaseUtils.getPrimaryProfile().getValue() as User
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

        // interestCards = ArrayList(cards.filter { it.likelihood <= 0 })

        interestCards = ArrayList(interestCards.sortedWith(compareByDescending({ (it.ratings_average - 3) * it.ratings_count + (it.timestamp / 86400) + it.likelihood })))
        interestCardObs.setValue(interestCards)
    }

    fun deleteCard(id: String) {
        var cards = interestCardsObservable.getValue()
        if (cards is List<*>) {
            interestCardsObservable.setValue(cards.filter { it is Card && it.id != id })
            FirebaseUtils.deleteData("cards/${id}")
        }
    }

    @Throws(IOException::class)
    fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir = File((Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).toString() + "/Rathings")
        if (!storageDir.exists())
            storageDir.mkdirs()
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }

    fun galleryAddPic(photo: File, context: Context) {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            mediaScanIntent.data = Uri.fromFile(photo)
            context.sendBroadcast(mediaScanIntent)
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