package com.example.rathings

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener



object FirebaseUtils {

    var userProfileObservable:CustomObservable=CustomObservable()
    var userCardsObservable:CustomObservable=CustomObservable()
    var interestCardsObservable:CustomObservable=CustomObservable()

    private var localUserProfile:User?=null

    fun getLocalUser():User? {return localUserProfile}

    val database = FirebaseDatabase.getInstance().reference

    @JvmStatic fun basePost() {
        database.child("users").child("TEST").child("username").setValue("Utente di Test")
    }

    @JvmStatic fun updateData(path:String, newData:MutableMap<String,Any>) {
        database.child(path).updateChildren(newData);
    }

    @JvmStatic fun createUserInstance(uid:String) {
        Log.d("[FIREBASE-UTILS]", "createUserInstance $uid")
        database.child("users").child(uid).child("id").setValue(uid)
    }

    @JvmStatic fun getProfile(uid:String?) {
        /**GET CURRENT AUTH USER IF UID IS NULL*/
        var id_user=uid
        if(uid==null)
            id_user=FirebaseAuth.getInstance().currentUser!!.uid

        val ref = FirebaseUtils.database.child("users")
        var user:User?
        val phoneQuery = ref.orderByChild("id").equalTo(id_user)

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    user = singleSnapshot.getValue(User::class.java)

                    localUserProfile=user
                    userProfileObservable.setValue(user)
                    Log.e("[FIREBASE-UTILS]", "onDataChange " + user?.toString())
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("[FIREBASE-UTILS]", "onCancelled", databaseError.toException())
            }
        }
        phoneQuery.addValueEventListener(postListener)
    }

    @JvmStatic fun getUserCards(uid:String?) {
        /**GET CURRENT AUTH USER IF UID IS NULL*/
        var id_user=uid
        if(uid==null)
            id_user=FirebaseAuth.getInstance().currentUser!!.uid

        val ref = FirebaseUtils.database.child("cards")
        var cards: ArrayList<Card> = ArrayList()
        val phoneQuery = ref.orderByChild("user").equalTo(id_user)

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    var card=singleSnapshot.getValue(Card::class.java)
                    if(card is Card)
                        cards.add(card)
                    Log.e("[FIREBASE-UTILS]", "onDataChange single card ${card.toString()}")
                }
                var sortedList = cards.sortedWith(compareBy({ it!!.timestamp }))
                userCardsObservable.setValue(sortedList)
                Log.e("[FIREBASE-UTILS]", "onDataChange UserCards ${cards}")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("[FIREBASE-UTILS]", "onCancelled", databaseError.toException())
            }
        }
        phoneQuery.addValueEventListener(postListener)
    }

    /**GET INTEREST CARDS
     * return only cards order by timestamp to the card controller
     * which get the collection and filter by interests and order by votes*/
    @JvmStatic fun getInterestCards(uid:String?) {
        /**GET CURRENT AUTH USER IF UID IS NULL*/
        var id_user=uid
        if(uid==null)
            id_user=FirebaseAuth.getInstance().currentUser!!.uid

        val ref = FirebaseUtils.database.child("cards")
        var cards: ArrayList<Card> = ArrayList()

        val query = ref.orderByChild("timestamp")

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    var card=singleSnapshot.getValue(Card::class.java)
                    if(card is Card)
                        cards.add(card)
                    Log.e("[FIREBASE-UTILS]", "onDataChange single card ${card.toString()}")
                }
                var sortedList = cards.sortedWith(compareBy({ it!!.timestamp }))
                interestCardsObservable.setValue(sortedList)
                Log.e("[FIREBASE-UTILS]", "onDataChange UserCards ${cards}")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("[FIREBASE-UTILS]", "onCancelled", databaseError.toException())
            }
        }
        query.addValueEventListener(postListener)
    }



}