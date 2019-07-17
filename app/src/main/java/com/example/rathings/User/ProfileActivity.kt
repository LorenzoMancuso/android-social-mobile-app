package com.example.rathings.User

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Half.toFloat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.rathings.Card.Card
import com.example.rathings.Card.CardAdapter
import com.example.rathings.FirebaseUtils
import com.example.rathings.Notification
import com.example.rathings.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import java.util.*
import kotlin.collections.ArrayList

class ProfileActivity : AppCompatActivity(), Observer {

    var primaryUserProfileObservable= FirebaseUtils.getPrimaryProfile()
    var localUserProfileObservable= FirebaseUtils.userProfileObservable
    var localUserCardsObservable= FirebaseUtils.userCardsObservable

    private var localUserProfile: User = User()
    private var localPrimaryUserProfile: User = User()

    private var cardRecyclerView: RecyclerView? = null
    private var cardAdapter: RecyclerView.Adapter<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        /**SET TOOLBAR OPTION*/
        val toolbar = findViewById<View>(R.id.toolbar) as androidx.appcompat.widget.Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayShowTitleEnabled(false)
        /**-----------------*/

        primaryUserProfileObservable.addObserver(this)
        localUserProfileObservable.addObserver(this)
        localUserCardsObservable.addObserver(this)

        /**BUTTONS*/
        findViewById<Button>(R.id.btn_follow)!!.setOnClickListener {addFollower()}
        findViewById<Button>(R.id.btn_unfollow)!!.setOnClickListener {removeFollower()}
        /**-----------------*/


        user_cards_recycler_view.isNestedScrollingEnabled = false

        val user=intent.getStringExtra("user");
        FirebaseUtils.getPrimaryProfile()
        //call for get profile info
        FirebaseUtils.getProfile(user)
        //call for get card of current user
        FirebaseUtils.getUserCards(user)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun addFollower(){
        if(localPrimaryUserProfile.id!="" && localUserProfile.id!=""){
            /**UPDATE FOLLOWED OF PRIMARY USER*/
            localPrimaryUserProfile.followed.add(localUserProfile.id)

            /**UPDATE FOLLOWERS OF OTHER USER*/
            localUserProfile.followers.add(localPrimaryUserProfile.id)

            FirebaseUtils.updateData(
                "users/${localPrimaryUserProfile.id}/",
                localPrimaryUserProfile.toMutableMap()
            )
            FirebaseUtils.updateData(
                "users/${localUserProfile.id}/",
                localUserProfile.toMutableMap()
            )
            addNotification(localUserProfile, localPrimaryUserProfile)
        }
        Log.d("[PROFILE-ACTIVITY]", "users/${localPrimaryUserProfile.id}/")
        Log.d("[PROFILE-ACTIVITY]", "users/${localUserProfile.id}/")
    }

    fun addNotification(userProfile: User, otherUserProfile: User){

        val timestamp = System.currentTimeMillis() / 1000L
        val split = userProfile.id.length/2
        val tmp = Notification("${timestamp}${userProfile.id.substring(split)}${otherUserProfile.id.substring(split)}",
            otherUserProfile.id,
            "${otherUserProfile.name} ${otherUserProfile.surname} started to follow you.",
            timestamp,
            false,
            "profile",
            userProfile.id)

        userProfile.notifiacations.add(tmp)
        FirebaseUtils.updateData(
            "users/${userProfile.id}/",
            localUserProfile.toMutableMap()
        )
    }

    fun removeFollower(){
        if(localPrimaryUserProfile.id!="" && localUserProfile.id!=""){

            /**UPDATE FOLLOWED OF PRIMARY USER*/
            localPrimaryUserProfile.followed = localPrimaryUserProfile.followed.filter { it != localUserProfile.id} as MutableList<Any>
            /**UPDATE FOLLOWERS OF OTHER USER*/
            localUserProfile.followers = localUserProfile.followers.filter { it != localPrimaryUserProfile.id} as MutableList<Any>

            FirebaseUtils.updateData(
                "users/${localPrimaryUserProfile.id}/",
                localPrimaryUserProfile.toMutableMap()
            )
            FirebaseUtils.updateData(
                "users/${localUserProfile.id}/",
                localUserProfile.toMutableMap()
            )
        }
        Log.d("[PROFILE-ACTIVITY]", "users/${localPrimaryUserProfile.id}/")
        Log.d("[PROFILE-ACTIVITY]", "users/${localUserProfile.id}/")
    }

    override fun update(observableObj: Observable?, data: Any?) {
        when(observableObj) {
            localUserProfileObservable -> {
                val value=localUserProfileObservable.getValue()
                if(value is User){
                    val user= value
                    localUserProfile=user
                    if(localPrimaryUserProfile.id!=""){
                        checkFollowRelation()
                    }
                    findViewById<TextView>(R.id.txt_name).text = "${user.name} ${user.surname}"
                    findViewById<TextView>(R.id.txt_profession).text = "${user.profession}"
                    findViewById<TextView>(R.id.txt_country).text = "${user.city}, ${user.country}"
                    findViewById<TextView>(R.id.txt_followers).text = "Followers: ${user.followers.size}"
                    findViewById<TextView>(R.id.txt_followed).text = "Followed: ${user.followed.size}"

                    val scale = resources.displayMetrics.density

                    if(profile_image!=null && user.profile_image != "") {
                        Picasso.get().load(user.profile_image).resize((200 * scale + 0.5f).toInt(), (200 * scale + 0.5f).toInt()).centerCrop().into(profile_image)
                    } else {
                        Picasso.get().load(R.drawable.default_avatar).resize((200 * scale + 0.5f).toInt(), (200 * scale + 0.5f).toInt()).centerCrop().into(profile_image)
                    }
                    Log.d("[PROFILE-FRAGMENT]", "PROFILE observable $user")
                }


            }
            localUserCardsObservable -> {
                val value = localUserCardsObservable.getValue()
                if (value is List<*>) {
                    val cards: ArrayList<Card> = ArrayList(value.filterIsInstance<Card>())
                    Log.d("[PROFILE-FRAGMENT]", "CARDS observable lenght ${cards.size}")
                    Log.d("[PROFILE-FRAGMENT]", "CARDS observable $cards")

                    findViewById<TextView>(R.id.txt_post).text = "${cards.size} cards"

                    val tmp = ArrayList(cards.filter { it.ratings_average > 0 })
                    val avg = tmp.map { card -> card.ratings_average }.average().toFloat()

                    findViewById<TextView>(R.id.txt_score).text = "Rathing ${Math.round((avg) * 10.0) / 10.0}"

                    cardRecyclerView = findViewById(R.id.user_cards_recycler_view)
                    val mLayoutManager = LinearLayoutManager(this,RecyclerView.VERTICAL,false)
                    cardRecyclerView?.layoutManager = mLayoutManager
                    cardAdapter = CardAdapter(cards)
                    cardRecyclerView?.adapter = cardAdapter
                }
            }
            primaryUserProfileObservable-> {
                val value=primaryUserProfileObservable.getValue()
                if(value is User){
                    val user= value
                    localPrimaryUserProfile=user
                    if(localUserProfile.id!=""){
                        checkFollowRelation()
                    }
                    Log.d("[PROFILE-FRAGMENT]", "PROFILE observable $user")
                }
            }
            else -> Log.d("[USER-CONTROLLER]", "observable not recognized $data")
        }
    }

    fun checkFollowRelation(){
        Log.d("[PROFILE-ACTIVITY]", "check follow relation")
        Log.d("[PROFILE-ACTIVITY]", "PRIMARY $localPrimaryUserProfile")
        Log.d("[PROFILE-ACTIVITY]", "OTHER $localUserProfile")
        if(localPrimaryUserProfile.followed.contains(localUserProfile.id)) {
            Log.d("[PROFILE-ACTIVITY]", "check follow relation is true")
            findViewById<Button>(R.id.btn_follow).visibility = View.GONE
            findViewById<Button>(R.id.btn_unfollow).visibility = View.VISIBLE
        } else {
            Log.d("[PROFILE-ACTIVITY]", "check follow relation is false")
            findViewById<Button>(R.id.btn_follow).visibility = View.VISIBLE
            findViewById<Button>(R.id.btn_unfollow).visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        localUserProfileObservable.deleteObserver(this)
        localUserCardsObservable.deleteObserver(this)
        primaryUserProfileObservable.deleteObserver(this)
    }
}
