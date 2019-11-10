package com.example.rathings.Card

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.viewpager.widget.ViewPager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.rathings.*
import com.example.rathings.Tab.Tab
import com.example.rathings.Tab.TabController
import com.example.rathings.Tab.TabsActivity
import com.example.rathings.User.ProfileActivity
import com.example.rathings.User.User
import com.example.rathings.utils.CustomObservable
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.fragment_profile.*
import java.util.*

class DetailedCardActivity : AppCompatActivity(), Observer, LinkPreviewFragment.OnFragmentInteractionListener {

    var tabsObs = TabController.tabsObs
    var cardsObs = FirebaseUtils.interestCardsObservable
    var selectedCard: Card = Card()

    override fun onFragmentInteraction(uri: Uri) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed_card)

        /**SET TOOLBAR OPTION*/
        val toolbar = findViewById<View>(R.id.toolbar) as androidx.appcompat.widget.Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        /**-----------------*/

        tabsObs.addObserver(this)
        cardsObs.addObserver(this)
        FirebaseUtils.getInterestCards(null)

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


    override fun update(observableObj: Observable?, data: Any?) {
        when(observableObj) {
            cardsObs -> {
                Log.d("[DETAILED CARD ACT]", "card observable")
                var valuesObs = cardsObs.getValue()
                if (valuesObs is List<*>) {
                    val cards: ArrayList<Card> = ArrayList(valuesObs.filterIsInstance<Card>())
                    val filteredCards = (cards.filter { it.id == intent.getStringExtra("idCard") })
                    if (filteredCards.isNotEmpty()) {
                        selectedCard = filteredCards[0]
                        Log.d("[DETAILED CARD ACT]", "selectedCard $selectedCard")
                        init()
                    } else {
                        finish()
                    }
                }
            }
            else -> Log.d("[DETAILED-CARD]", "observable not recognized $data")
        }
    }

    private fun init() {
        // User, Title, Description, Categories, Link, Multimedia, Comments and SettingsButton
        Log.d("[DETAILED CARD ACT]", "Init")

        /**init methods*/
        initUser() //OK
        initCategories()//OK
        initLink() //OK
        initMultimedia() //OK
        initSettingsButton() //OK

        Log.d("[DETAILED CARD ACT]", "End init")

        findViewById<TextView>(R.id.title).text = selectedCard.title
        findViewById<TextView>(R.id.description).text = selectedCard.description

        // Date
        val date = Date(selectedCard.timestamp.toLong() * 1000)
        (findViewById(R.id.date) as TextView).text = java.text.SimpleDateFormat("dd-MM-yyyy' - 'HH:mm", Locale.ITALY).format(date)

        // Profile Image
        Log.e("[DETAILED-CARD]", selectedCard.userObj.profile_image)
        if(profile_image != null && selectedCard.userObj.profile_image != "") {
            Glide.with(this).load(selectedCard.userObj.profile_image)
                .centerCrop().circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(profile_image)
        }

        Log.d("[DETAILED CARD ACT]", "Final init")
        // To initialize Rating Bar
        var userObj = FirebaseUtils.primaryUserProfileObservable.getValue() as User

        Log.d("[DETAILED CARD ACT]", "get user")

        initRatingBar(userObj)
        Log.d("[DETAILED CARD ACT]", "end rating bar init")
        initComments(userObj)
        Log.d("[DETAILED CARD ACT]", "end comment init")

    }

    fun initUser() {
        findViewById<TextView>(R.id.user).text = this.resources.getString(R.string.name_surname, selectedCard.userObj.name, selectedCard.userObj.surname)

        findViewById<TextView>(R.id.user)!!.setOnClickListener {
            val uid = selectedCard.userObj.id
            if(FirebaseUtils.isCurrentUser(uid)){
                val intent = Intent(it.context, HomeActivity::class.java)
                intent.putExtra("mode", "profile")
                it.context.startActivity(intent)
            }else{
                val intent = Intent(it.context, ProfileActivity::class.java)
                intent.putExtra("user", uid)
                it.context.startActivity(intent)
            }
        }
    }

    fun initRatingBar(user: User) {
        var ratingBar = findViewById<RatingBar>(R.id.ratings)
        var finalRatingBar = findViewById<RatingBar>(R.id.final_ratings)
        ratingBar.rating = selectedCard.ratings_average
        finalRatingBar.rating = selectedCard.ratings_average

        // Show the FinalRatingBar if the user has already voted in the past
        if(selectedCard.ratings_users.containsKey(user.id)) {
            ratingBar.visibility = View.GONE
            finalRatingBar.visibility = View.VISIBLE
            findViewById<TextView>(R.id.ratings_title).text = this.getString(R.string.rate_text)
        }

        ratingBar.setOnRatingBarChangeListener { ratings, value, fromUser ->
            run {
                if (fromUser) {
                    findViewById<TextView>(R.id.ratings_title).text = this.getString(R.string.rate_text)

                    // Calc the average
                    selectedCard.ratings_average = ((selectedCard.ratings_average * selectedCard.ratings_count) + value) / (selectedCard.ratings_count + 1)
                    selectedCard.ratings_count++
                    selectedCard.ratings_users.set(user.id, value)

                    Toast.makeText(this, this.getString(R.string.toast_rate_response), Toast.LENGTH_SHORT).show()

                    // Set new Value on RatingBar
                    ratings.rating = selectedCard.ratings_average
                    finalRatingBar.rating = selectedCard.ratings_average

                    // Set Visibility after rate
                    ratingBar.visibility = View.GONE
                    finalRatingBar.visibility = View.VISIBLE

                    // Save data in Firebase
                    FirebaseUtils.updateData("cards/${selectedCard.id}/",selectedCard.toMutableMap())

                    addNotification(user, selectedCard, "rating")
                }
                Log.d("[RATING-BAR]", "New value = ${value} , fromUser = ${fromUser}")
            }
        }
    }

    fun initMultimedia() {
        val scale = resources.displayMetrics.density
        val containerMultimedia = findViewById<LinearLayout>(R.id.container_multimedia)
        containerMultimedia.removeAllViews()

        var newLinearLayout = LinearLayout(applicationContext)
        newLinearLayout.orientation = LinearLayout.HORIZONTAL
        newLinearLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
        containerMultimedia.addView(newLinearLayout)

        var paramsRow : LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)

        if (selectedCard.multimedia.size == 1) {
            var row = containerMultimedia.getChildAt(containerMultimedia.childCount - 1) as LinearLayout

            var imageView = ImageView(applicationContext)
            imageView.setPadding(5,5,5,5)
            imageView.layoutParams = LinearLayout.LayoutParams((300 * scale + 0.5f).toInt(), (300 * scale + 0.5f).toInt(), 1F)

            Glide.with(this).load(selectedCard.multimedia[0])
                .centerCrop().centerInside()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)

            row.addView(imageView)
        } else if (selectedCard.multimedia.size > 0) {

            for (i in selectedCard.multimedia.indices) {
                var row = containerMultimedia.getChildAt(containerMultimedia.childCount - 1) as LinearLayout
                row.setOnClickListener{ openMultimediaActivity() }

                if (row.childCount == 2) {
                    row = LinearLayout(applicationContext)
                    row.layoutParams = paramsRow
                    row.orientation = LinearLayout.HORIZONTAL
                    containerMultimedia.addView(row)
                }

                manageMedia(row, selectedCard.multimedia[i])
            }
        }
    }

    fun manageMedia(row: LinearLayout, imagePath: String) {
        val scale = resources.displayMetrics.density

        var imageView = ImageView(applicationContext)
        imageView.setPadding(5,5,5,5)
        imageView.layoutParams = LinearLayout.LayoutParams((150 * scale + 0.5f).toInt(), (150 * scale + 0.5f).toInt(), 1F)

        Glide.with(this).load(imagePath)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)

        row.addView(imageView)
    }

    fun openMultimediaActivity() {
        val intent = Intent(this, MultimediaActivity::class.java)
        intent.putStringArrayListExtra("multimedia", selectedCard.multimedia as ArrayList<String>)
        startActivityForResult(intent, 1)
    }

    fun initComments(user: User) {
        var cardRecyclerView = findViewById<RecyclerView>(R.id.recycler_comments)
        val publishComment = findViewById<MaterialButton>(R.id.publish_comment)

        val mLayoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL,false)
        cardRecyclerView.layoutManager = mLayoutManager
        var commentAdapter = CommentAdapter(selectedCard.comments as ArrayList<Comment>)
        cardRecyclerView.adapter = commentAdapter

        publishComment.setOnClickListener(View.OnClickListener { addComment(user) })
    }

    fun initCategories() {
        var containerCategories = findViewById<ChipGroup>(R.id.container_categories)
        containerCategories.removeAllViews()
        var tabs = tabsObs.getValue() as ArrayList<Tab>
        for (i in 0 until selectedCard.category.size) {
            for (j in 0 until tabs.size) {
                if (selectedCard.category[i] == tabs[j].id) {
                    var chip = Chip(this)
                    chip.text = tabs[j].value
                    chip.chipBackgroundColor = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_enabled)), intArrayOf(Color.parseColor(tabs[j].color)))
                    chip.setTextColor(Color.WHITE)
                    containerCategories.addView(chip)
                }
            }
        }
    }

    fun initLink() {
        if (selectedCard.link != "") {
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            val arguments = Bundle()
            var linkPreviewFragment = LinkPreviewFragment()
            arguments.putString("URL", selectedCard.link)
            linkPreviewFragment.arguments = arguments
            fragmentTransaction.replace(R.id.container_link, linkPreviewFragment)
            fragmentTransaction.commit()
        }
    }

    fun initSettingsButton() {
        if (FirebaseUtils.isCurrentUser(selectedCard.userObj.id)) {
            findViewById<MaterialButton>(R.id.settings_button).visibility = View.VISIBLE

            var settingsButton = findViewById<Button>(R.id.settings_button)
            settingsButton.setOnClickListener(View.OnClickListener() {
                var popup = PopupMenu(this, settingsButton)
                popup.menuInflater.inflate(R.menu.settings_detailed_card, popup.menu)

                popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener() {
                    if (it.title == this.getString(R.string.settings_detailed_card_edit)) {
                        editCard()
                    } else if (it.title == this.getString(R.string.settings_detailed_card_delete)) {
                        deleteCard()
                    }
                    true
                })
                popup.show()
            })
        }
    }

    fun editCard() {
        val intent = Intent(applicationContext, EditCardActivity::class.java)
        intent.putExtra("card", selectedCard)
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        applicationContext.startActivity(intent)
        Log.d("[AFTER ACTIVITY]", "Init Card")
        init()
    }

    fun deleteCard() {
        CardController.deleteCard(selectedCard.id)
    }

    fun addComment(user: User) {
        var comment = findViewById<EditText>(R.id.added_comment)
        if (comment.text.toString() != "") {
            var newComment = Comment()
            newComment.id = selectedCard.comments.size.toLong()
            newComment.userObj = user
            newComment.user = newComment.userObj.id
            newComment.text = comment.text.toString()
            newComment.timestamp = (System.currentTimeMillis() / 1000).toInt()

            FirebaseUtils.updateData("cards/${selectedCard.id}/comments/${newComment.id }", newComment.toMutableMap())
            Log.d("[DETAILED CARD ACT]", "UPDATED Card")
            Toast.makeText(this, this.getString(R.string.add_comment_done), Toast.LENGTH_SHORT)

            comment.setText("")

            addNotification(user, selectedCard, "comment")

        } else {
            Toast.makeText(this, this.getString(R.string.comment_toast_error), Toast.LENGTH_SHORT)
        }
    }

    fun addNotification(userProfile: User, card: Card, type:String){
        val otherUserProfile = card.userObj

        val timestamp = System.currentTimeMillis() / 1000L
        val split = userProfile.id.length/2

        val tmp = Notification("${timestamp}${userProfile.id.substring(split)}${otherUserProfile.id.substring(split)}",
            userProfile.id,
            "${userProfile.name} ${userProfile.surname} added a ${type} to your card.",
            timestamp,
            false,
            "card",
            card.id)

        otherUserProfile.notifications.add(tmp)
        FirebaseUtils.updateData(
            "users/${otherUserProfile.id}/",
            otherUserProfile.toMutableMap()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        tabsObs.deleteObserver(this)
        cardsObs.deleteObserver(this)
    }

}
