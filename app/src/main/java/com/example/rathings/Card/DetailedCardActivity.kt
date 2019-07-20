package com.example.rathings.Card

import android.content.Intent
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
    var userObs = CustomObservable()
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
                var valuesObs = cardsObs.getValue()
                if (valuesObs is List<*>) {
                    val cards: ArrayList<Card> = ArrayList(valuesObs.filterIsInstance<Card>())
                    val filteredCards = (cards.filter { it.id == intent.getStringExtra("idCard") })
                    if (filteredCards.isNotEmpty()) {
                        selectedCard = filteredCards[0]
                        init()
                    } else {
                        finish()
                    }
                }
            }
            userObs -> {
                initRatingBar(userObs.getValue() as User)
                initComments(userObs.getValue() as User)
            }
            else -> Log.d("[DETAILED-CARD]", "observable not recognized $data")
        }
    }

    private fun init() {
        // User, Title, Description, Categories, Link, Multimedia, Comments and SettingsButton
        initUser()
        findViewById<TextView>(R.id.title).text = selectedCard.title
        findViewById<TextView>(R.id.description).text = selectedCard.description
        initCategories()
        initLink()
        initMultimedia()
        initSettingsButton()

        // Date
        val date = Date(selectedCard.timestamp.toLong() * 1000)
        (findViewById(R.id.date) as TextView).text = java.text.SimpleDateFormat("dd-MM-yyyy' - 'HH:mm", Locale.ITALY).format(date)

        // Profile Image
        Log.e("[DETAILED-CARD]", selectedCard.userObj.profile_image)
        if(profile_image != null && selectedCard.userObj.profile_image != "") {
            Picasso.get().load(selectedCard.userObj.profile_image).into(profile_image)
        }

        // To initialize Rating Bar
        userObs = FirebaseUtils.getPrimaryProfile()
        userObs.addObserver(this)

    }

    fun initUser() {
        findViewById<TextView>(R.id.user).text = "${selectedCard.userObj.name} ${selectedCard.userObj.surname}"

        findViewById<TextView>(R.id.user)!!.setOnClickListener {
            val uid = selectedCard.userObj.id
            if(FirebaseUtils.isCurrentUser(uid)){
                val intent = Intent(it.context, HomeActivity::class.java)
                intent.putExtra("mode", "profile");
                it.context.startActivity(intent)
            }else{
                val intent = Intent(it.context, ProfileActivity::class.java)
                intent.putExtra("user", uid);
                it.context.startActivity(intent)
            }
        }
    }

    fun initRatingBar(user: User) {
        var ratingBar = findViewById(R.id.ratings) as RatingBar
        var finalRatingBar = findViewById(R.id.final_ratings) as RatingBar
        ratingBar.rating = selectedCard.ratings_average
        finalRatingBar.rating = selectedCard.ratings_average

        // Show the FinalRatingBar if the user has already voted in the past
        if(selectedCard.ratings_users.containsKey(user.id)) {
            ratingBar.visibility = View.GONE
            finalRatingBar.visibility = View.VISIBLE
            findViewById<TextView>(R.id.ratings_title).text = "Ratings average"
        }

        ratingBar.setOnRatingBarChangeListener { ratings, value, fromUser ->
            run {
                if (fromUser) {
                    findViewById<TextView>(R.id.ratings_title).text = "Ratings average"

                    // Calc the average
                    selectedCard.ratings_average = ((selectedCard.ratings_average * selectedCard.ratings_count) + value) / (selectedCard.ratings_count + 1)
                    selectedCard.ratings_count++
                    selectedCard.ratings_users.set(user.id, value)

                    Toast.makeText(this, "Thanks for your Rate.", Toast.LENGTH_SHORT).show()

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

            Picasso.get().load(selectedCard.multimedia[0]).resize((300 * scale + 0.5f).toInt(), (300 * scale + 0.5f).toInt()).onlyScaleDown().centerInside().into(imageView)
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

                if (selectedCard.multimedia[i].contains("video")) { // If media is a video, setThumbnail to imageView and user ExoPlayer with disabled controls
                    manageVideo(row, selectedCard.multimedia[i])

                } else if (selectedCard.multimedia[i].contains("image")) { // else, set image
                    manageImage(row, selectedCard.multimedia[i])
                }
            }
        }
    }

    var listOfVideoPlayers: ArrayList<ExoPlayer> = ArrayList()
    fun manageVideo(row: LinearLayout, videoPath: String) {
        val scale = resources.displayMetrics.density

        var playerView = PlayerView(applicationContext)
        val player = ExoPlayerFactory.newSimpleInstance(applicationContext,  DefaultTrackSelector())
        var mediaSource = ExtractorMediaSource.Factory(DefaultDataSourceFactory(applicationContext, "rathings")).createMediaSource(Uri.parse(videoPath))
        var thumbnail = ImageView(applicationContext)

        listOfVideoPlayers.add(player)
        playerView.layoutParams = LinearLayout.LayoutParams((150 * scale + 0.5f).toInt(), (150 * scale + 0.5f).toInt(), 1F)
        playerView.setPadding(5,5,5,5)
        playerView.player = player
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM)
        playerView.useController = false

        thumbnail.setBackgroundColor(Color.parseColor("#90111111"))
        thumbnail.setImageResource(R.drawable.ic_slow_motion_video_white_48dp)
        thumbnail.scaleType = ImageView.ScaleType.CENTER_INSIDE

        playerView.overlayFrameLayout.addView(thumbnail)
        player.prepare(mediaSource)

        row.addView(playerView)
    }

    fun manageImage(row: LinearLayout, imagePath: String) {
        val scale = resources.displayMetrics.density

        var imageView = ImageView(applicationContext)
        imageView.setPadding(5,5,5,5)
        imageView.layoutParams = LinearLayout.LayoutParams((150 * scale + 0.5f).toInt(), (150 * scale + 0.5f).toInt(), 1F)

        Picasso.get().load(imagePath).centerCrop().fit().into(imageView)
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
        var containerCategories = findViewById(R.id.container_categories) as ChipGroup
        containerCategories.removeAllViews()
        var tabs = tabsObs.getValue() as ArrayList<Tab>
        for (i in 0 until selectedCard.category.size) {
            for (j in 0 until tabs.size) {
                if (selectedCard.category[i] == tabs[j].id.toInt()) {
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
            val linkPreviewFragment = LinkPreviewFragment()
            val arguments = Bundle()
            var containerLink = findViewById(R.id.container_link) as LinearLayout
            containerLink.removeAllViews()
            arguments.putString("URL", selectedCard.link)
            linkPreviewFragment.setArguments(arguments)
            fragmentTransaction.add(R.id.container_link, linkPreviewFragment)
            fragmentTransaction.commit()
        }
    }

    fun initSettingsButton() {
        if (FirebaseUtils.isCurrentUser(selectedCard.userObj.id)) {
            findViewById<MaterialButton>(R.id.settings_button).visibility = View.VISIBLE

            var settingsButton = findViewById(R.id.settings_button) as Button
            settingsButton.setOnClickListener(View.OnClickListener() {
                var popup = PopupMenu(this, settingsButton);
                popup.getMenuInflater().inflate(R.menu.settings_detailed_card, popup.getMenu())

                popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener() {
                    if (it.title == "Edit Card") {
                        editCard()
                    } else if (it.title == "Delete Card") {
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
        applicationContext.startActivity(intent)
        init()
    }

    fun deleteCard() {
        CardController.deleteCard(selectedCard.id)
    }

    fun addComment(user: User) {
        var comment = (findViewById( R.id.added_comment) as EditText)
        if (comment.text.toString() != "") {
            var newComment = Comment()
            newComment.id = selectedCard.comments.size.toLong()
            newComment.userObj = user
            newComment.user = newComment.userObj.id
            newComment.text = comment.text.toString()
            newComment.timestamp = (System.currentTimeMillis() / 1000).toInt()

            FirebaseUtils.updateData("cards/${selectedCard.id}/comments/${newComment.id }", newComment.toMutableMap())
            Toast.makeText(this, "Add Comment done.", Toast.LENGTH_SHORT)

            comment.setText("")

            addNotification(user, selectedCard, "comment")

        } else {
            Toast.makeText(this, "Comment is mandatory.", Toast.LENGTH_SHORT)
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
        userObs.deleteObserver(this)
        if (listOfVideoPlayers.size > 0) {
            for (player in listOfVideoPlayers) {
                player.release()
            }
        }
    }

}
