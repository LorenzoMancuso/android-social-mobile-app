package com.example.rathings

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Button
import android.widget.TextView
import java.util.*

class ProfileActivity : AppCompatActivity(), Observer {

    var localPrimaryUserProfileObservable=FirebaseUtils.primaryUserProfileObservable
    var localUserProfileObservable=FirebaseUtils.userProfileObservable
    var localUserCardsObservable=FirebaseUtils.userCardsObservable

    private var localUserProfile:User=User()
    private var localPrimaryUserProfile:User=User()

    private var cardRecyclerView: RecyclerView? = null
    private var cardAdapter: RecyclerView.Adapter<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        localPrimaryUserProfileObservable.addObserver(this)
        localUserProfileObservable.addObserver(this)
        localUserCardsObservable.addObserver(this)

        findViewById<Button>(R.id.btn_follow).isEnabled=false
        findViewById<Button>(R.id.btn_follow)!!.setOnClickListener {addFollower()}

        val user=intent.getStringExtra("user");
        FirebaseUtils.getPrimaryProfile()
        //call for get profile info
        FirebaseUtils.getProfile(user)
        //call for get card of current user
        FirebaseUtils.getUserCards(user)
    }

    fun addFollower(){
        if(localPrimaryUserProfile.id!="" && localUserProfile.id!=""){
            /**UPDATE FOLLOWED OF PRIMARY USER*/
            localPrimaryUserProfile.followed.add(localUserProfile.id)

            /**UPDATE FOLLOWERS OF OTHER USER*/
            localUserProfile.followers.add(localPrimaryUserProfile.id)

            FirebaseUtils.updateData("users/${localPrimaryUserProfile.id}/",localPrimaryUserProfile.toMutableMap())
            FirebaseUtils.updateData("users/${localUserProfile.id}/",localUserProfile.toMutableMap())
        }
        Log.d("[PROFILE-ACTIVITY]", "users/${localPrimaryUserProfile.id}/")
        Log.d("[PROFILE-ACTIVITY]", "users/${localUserProfile.id}/")
    }

    fun checkFollowRelation(){
        if(!(localUserProfile.id in localPrimaryUserProfile.followed))
            findViewById<Button>(R.id.btn_follow).isEnabled=true
    }

    override fun update(observableObj: Observable?, data: Any?) {
        when(observableObj) {
            localUserProfileObservable -> {
                val value=localUserProfileObservable.getValue()
                if(value is User){
                    val user= value
                    localUserProfile=user
                    if(localPrimaryUserProfile.id!=""){checkFollowRelation()}
                    findViewById<TextView>(R.id.txt_name).text = "${user.name} ${user.surname}"
                    findViewById<TextView>(R.id.txt_profession).text = "${user.profession}"
                    findViewById<TextView>(R.id.txt_country).text = "${user.city}, ${user.country}"
                    findViewById<TextView>(R.id.txt_followers).text = "Followers: ${user.followers.size}"
                    findViewById<TextView>(R.id.txt_followed).text = "Followed: ${user.followed.size}"
                    Log.d("[PROFILE-FRAGMENT]", "PROFILE observable $user")
                }


            }
            localUserCardsObservable -> {
                val value = localUserCardsObservable.getValue()
                if (value is List<*>) {
                    val cards: ArrayList<Card> = ArrayList(value.filterIsInstance<Card>())
                    Log.d("[PROFILE-FRAGMENT]", "CARDS observable $cards")
                    cardRecyclerView = findViewById(R.id.user_cards_recycler_view)
                    val mLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                    cardRecyclerView?.layoutManager = mLayoutManager
                    cardAdapter = CardAdapter(cards)
                    cardRecyclerView?.adapter = cardAdapter
                }
            }
            localPrimaryUserProfileObservable-> {
                val value=localUserProfileObservable.getValue()
                if(value is User){
                    val user= value
                    localPrimaryUserProfile=user
                    if(localUserProfile.id!=""){checkFollowRelation()}
                    Log.d("[PROFILE-FRAGMENT]", "PROFILE observable $user")
                }
            }
            else -> Log.d("[USER-CONTROLLER]", "observable not recognized $data")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        localUserProfileObservable.deleteObserver(this)
        localUserCardsObservable.deleteObserver(this)
        localPrimaryUserProfileObservable.deleteObserver(this)
    }
}
