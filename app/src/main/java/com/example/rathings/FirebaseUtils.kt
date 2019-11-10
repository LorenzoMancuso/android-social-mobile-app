package com.example.rathings

import android.util.Log
import com.example.rathings.Card.Card
import com.example.rathings.Tab.Tab
import com.example.rathings.User.User
import com.example.rathings.utils.CustomObservable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList


object FirebaseUtils {

    var primaryUserProfileObservable: CustomObservable = CustomObservable()
    private var allUsers: CustomObservable = CustomObservable()

    var userProfileObservable: CustomObservable = CustomObservable()

    var userCardsObservable: CustomObservable = CustomObservable()
    var interestCardsObservable: CustomObservable = CustomObservable()

    var tabsObservable: CustomObservable = CustomObservable()
    var notificationsObservable: CustomObservable = CustomObservable()

    private var auth: FirebaseAuth

    val database = FirebaseDatabase.getInstance().reference

    init{
        auth = FirebaseAuth.getInstance()
    }


    // ----------------------
    // BEGIN Firebase methods
    // ----------------------
    @JvmStatic fun basePost() {
        database.child("users").child("TEST").child("username").setValue("Utente di Test")
    }

    @JvmStatic fun setData(path:String, newData:MutableMap<String,Any>) {
        database.child(path).setValue(newData)
    }

    @JvmStatic fun updateData(path:String, newData:MutableMap<String,Any>) {
        database.child(path).updateChildren(newData);
    }

    @JvmStatic fun deleteData(path:String) {
        database.child(path).removeValue()
    }


    // ------------------
    // BEGIN User methods
    // ------------------
    @JvmStatic fun createUserInstance(uid:String): CustomObservable {
        // database.child("users").child(uid).child("id").setValue(uid)

        database.child("users").orderByChild("id").equalTo(uid).addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if(dataSnapshot.children.count()==0){
                        database.child("users").child(uid).child("id").setValue(uid)
                        database.child("users").child(uid).child("subscription_date").setValue(System.currentTimeMillis() / 1000L)
                    }
                }
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            }
        )
        return primaryUserProfileObservable
    }

    fun isCurrentUser(user_id:String):Boolean {
        val currentUser = auth.currentUser
        Log.d("[FIREBASE-UTILS]", "currentUser ${currentUser?.uid}, userParam $user_id")
        if (currentUser?.uid == user_id) {
            return true
        }
        return false
    }

    // ---------------------------------
    // BEGIN Wrappers for UserController
    // ---------------------------------
    fun getUsers(): CustomObservable {
        /**GET CURRENT AUTH USER IF UID IS NULL*/
        val ref = FirebaseUtils.database.child("users")
        var user: User?
        val phoneQuery = ref.orderByChild("id")

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var listOfUsers: ArrayList<User?> = ArrayList()

                for (singleSnapshot in dataSnapshot.children) {
                    user = singleSnapshot.getValue(User::class.java)
                    listOfUsers.add(user)
                    Log.e("[FIREBASE-UTILS]", "Search User " + user?.toString())
                }

                allUsers.setValue(listOfUsers)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("[FIREBASE-UTILS]", "onCancelled", databaseError.toException())
            }
        }
        phoneQuery.addValueEventListener(postListener)
        return allUsers
    }

    @JvmStatic fun getPrimaryProfile(): CustomObservable {
        val uid=FirebaseAuth.getInstance().currentUser!!.uid

        val ref = FirebaseUtils.database.child("users")
        var user: User?
        val phoneQuery = ref.orderByChild("id").equalTo(uid)

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    user = singleSnapshot.getValue(User::class.java)

                    primaryUserProfileObservable.setValue(user)
                    Log.e("[FIREBASE-UTILS]", "primaryUser " + user?.toString())
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("[FIREBASE-UTILS]", "onCancelled", databaseError.toException())
            }
        }
        phoneQuery.addValueEventListener(postListener)
        return primaryUserProfileObservable
    }

    @JvmStatic fun getProfile(uid:String?): CustomObservable {
        /**GET CURRENT AUTH USER IF UID IS NULL*/
        var id_user=uid

        val ref = FirebaseUtils.database.child("users")
        var user: User?
        val phoneQuery = ref.orderByChild("id").equalTo(id_user)

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.children.count()==0){
                    userProfileObservable.setValue(null)
                } else {
                    for (singleSnapshot in dataSnapshot.children) {
                        user = singleSnapshot.getValue(User::class.java)

                        userProfileObservable.setValue(user)
                        Log.e("[FIREBASE-UTILS]", "getProfile " + user?.toString())
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("[FIREBASE-UTILS]", "onCancelled", databaseError.toException())
            }
        }
        phoneQuery.addValueEventListener(postListener)
        return userProfileObservable
    }


    // --------------------------------
    // BEGIN Wrappers for TabController
    // --------------------------------
    @JvmStatic fun getTabs() {
        val ref = FirebaseUtils.database.child("tabs")

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val tempArray: ArrayList<Tab?> = ArrayList()
                for (singleSnapshot in dataSnapshot.children) {
                    val tab = singleSnapshot.getValue(Tab::class.java)
                    tempArray.add(tab)
                }
                tabsObservable.setValue(tempArray)
                Log.e("[FIREBASE-UTILS]", "tabs " + tempArray)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("[FIREBASE-UTILS]", "onCancelled", databaseError.toException())
            }
        }
        ref.addValueEventListener(postListener)
    }


    // ---------------------------------
    // BEGIN Wrappers for CardController
    // ---------------------------------
    @JvmStatic fun getUserCards(uid:String?) {
        /**GET CURRENT AUTH USER IF UID IS NULL*/
        var id_user=uid
        if(uid==null)
            id_user=FirebaseAuth.getInstance().currentUser!!.uid

        Log.e("[FIREBASE-UTILS-USER]", "user uid ${id_user}")
        val ref = FirebaseUtils.database.child("cards")

        val phoneQuery = ref.orderByChild("user").equalTo(id_user)

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var cards: ArrayList<Card> = ArrayList()

                for (singleSnapshot in dataSnapshot.children) {
                    var card=singleSnapshot.getValue(Card::class.java)
                    if(card is Card)
                        cards.add(card)
                    Log.e("[FIREBASE-UTILS-USER]", "onDataChange single card ${card.toString()}")
                }
                var sortedList = cards.sortedWith(compareBy({ it!!.timestamp }))

                /**GET USER OBJECT FOR EACH CARD*/
                FirebaseUtils.database.child("users").addValueEventListener(
                    object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (singleSnapshot in dataSnapshot.children) {
                                var user=singleSnapshot.getValue(User::class.java)
                                if(user is User){
                                    for(card in sortedList){
                                        if (card.user==user.id) {
                                            card.userObj=user
                                        }
                                    }
                                }
                            }
                            userCardsObservable.setValue(sortedList)
                            Log.e("[FIREBASE-UTILS]", "onDataChange UserCards ${cards}")
                        }
                        override fun onCancelled(p0: DatabaseError) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }
                    }
                )
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
        val ref = FirebaseUtils.database.child("cards")

        val query = ref.orderByChild("timestamp")

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var cards: ArrayList<Card> = ArrayList()

                for (singleSnapshot in dataSnapshot.children) {
                    var card=singleSnapshot.getValue(Card::class.java)
                    if(card is Card)
                        cards.add(card)
                }
                val sortedList = cards.sortedWith(compareBy({ it.timestamp }))

                /**GET USER OBJECT FOR EACH CARD*/
                FirebaseUtils.database.child("users").addValueEventListener(
                    object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (singleSnapshot in dataSnapshot.children) {
                                var user=singleSnapshot.getValue(User::class.java)
                                if(user is User){
                                    for(card in sortedList){
                                        if (card.user==user.id) {
                                            card.userObj=user
                                            Log.e("[FIREBASE-UTILS]", "onDataChange card ${card.toString()}")

                                        }
                                        for (comment in card.comments) {
                                            if (comment.user == user.id) {
                                                comment.userObj = user
                                            }
                                        }
                                    }
                                }
                            }
                            interestCardsObservable.setValue(sortedList)
                        }
                        override fun onCancelled(p0: DatabaseError) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }
                    }
                )
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("[FIREBASE-UTILS]", "onCancelled", databaseError.toException())
            }
        }
        query.addValueEventListener(postListener)
    }

    /**GET NOTIFICATION*/
    fun getNotificationsOld(): CustomObservable {

        getPrimaryProfile().addObserver(object: Observer {
            override fun update(o: Observable?, arg: Any?) {
                var user = (o as CustomObservable).getValue() as User
                notificationsObservable.setValue(user.notifications)
            }
        })

        return notificationsObservable

    }

    fun getNotifications(): CustomObservable {
        var user: User
        try {
            user = getPrimaryProfile().getValue() as User
            /**Subscribe user's notifications only for update pourpose, all notifications are already in user object*/
            FirebaseUtils.database.child("users/${user.id}/notifications").addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        Log.d("[NOTIFICATION OBS]", user.notifications.toString())
                        Log.d("[NOTIFICATION OBS]", user.notifications.sortedWith(compareByDescending({ it.timestamp })).toString())

                        val notifications = ArrayList<Notification>()
                        for (singleSnapshot in dataSnapshot.children) {
                            var notification=singleSnapshot.getValue(Notification::class.java)
                            if(notification is Notification){
                                notifications.add(notification)
                            }
                        }
                        // user.notifications = notifications.sortedWith(compareByDescending({ it.timestamp })).toMutableList()
                        notificationsObservable.setValue(ArrayList(notifications.sortedWith(compareByDescending({ it.timestamp }))))
                    }
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }
                })
        } catch (e:Exception) {
            Log.e("[FIREBASE-UTILS]", "getNotifications error")
            notificationsObservable.setValue(ArrayList<Notification>())
        }

        return notificationsObservable
    }
}