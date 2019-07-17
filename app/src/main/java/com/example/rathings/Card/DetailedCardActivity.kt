package com.example.rathings.Card

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
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
                    selectedCard = (cards.filter { it.id == intent.getStringExtra("idCard") })[0]
                    initData()
                }
            }
            userObs -> {
                setRatingBar(userObs.getValue() as User)
                setComments(userObs.getValue() as User)
            }
            else -> Log.d("[DETAILED-CARD]", "observable not recognized $data")
        }
    }

    private fun initData() {
        // User, Title, Description, Categories, Link, Multimedia, Comments and SettingsButton
        setUser()
        (findViewById(R.id.title) as TextView).text = selectedCard.title
        (findViewById(R.id.description) as TextView).text = selectedCard.description
        setCategories()
        setLink()
        setMultimedia()
        setSettingsButton()

        // Date
        val date = Date(selectedCard.timestamp.toLong() * 1000)
        (findViewById(R.id.date) as TextView).text = java.text.SimpleDateFormat("yyyy-MM-dd' - 'HH:mm:ss", Locale.ITALY).format(date)

        // Profile Image
        Log.e("[DETAILED-CARD]", selectedCard.userObj.profile_image)
        if(profile_image != null && selectedCard.userObj.profile_image != "") {
            Picasso.get().load(selectedCard.userObj.profile_image).into(profile_image)
        }

        // To initialize Rating Bar
        userObs = FirebaseUtils.getPrimaryProfile()
        userObs.addObserver(this)

    }

    fun setUser() {
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

    fun setRatingBar(user: User) {
        var ratingBar = findViewById(R.id.ratings) as RatingBar
        var finalRatingBar = findViewById(R.id.final_ratings) as RatingBar
        ratingBar.rating = selectedCard.ratings_average
        finalRatingBar.rating = selectedCard.ratings_average

        // Show the FinalRatingBar if the user has already voted in the past
        Log.e("[VOTES Users]", selectedCard.ratings_users.toString())
        Log.e("[VOTES Average]", selectedCard.ratings_average.toString())
        Log.e("[VOTES Count]", selectedCard.ratings_count.toString())
        if(selectedCard.ratings_users.containsKey(user.id)) {
            ratingBar.visibility = View.GONE
            finalRatingBar.visibility = View.VISIBLE
            (findViewById(R.id.ratings_title) as TextView).text = "Ratings average"
        }

        ratingBar.setOnRatingBarChangeListener { ratings, value, fromUser ->
            run {
                if (fromUser) {
                    (findViewById(R.id.ratings_title) as TextView).text = "Ratings average"

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

    fun setMultimedia() {
        val containerMultimedia = findViewById(R.id.container_multimedia) as LinearLayout
        containerMultimedia.removeAllViews()
        var newLinearLayout = LinearLayout(applicationContext)
        newLinearLayout.orientation = LinearLayout.HORIZONTAL
        newLinearLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
        containerMultimedia.addView(newLinearLayout)

        val scale = resources.displayMetrics.density
        var paramsRow : LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
        var paramsImage = LinearLayout.LayoutParams((150 * scale + 0.5f).toInt(), (150 * scale + 0.5f).toInt(), 1F)

        if (selectedCard.multimedia.size == 1) {
            var row = containerMultimedia.getChildAt(containerMultimedia.childCount - 1) as LinearLayout

            var imageView = ImageView(applicationContext)
            imageView.setPadding(5,5,5,5)

            Picasso.get().load(selectedCard.multimedia[0]).resize((300 * scale + 0.5f).toInt(), (300 * scale + 0.5f).toInt()).onlyScaleDown().centerInside().into(imageView)
            row.addView(imageView)
        } else if (selectedCard.multimedia.size > 0) {

            for (i in selectedCard.multimedia.indices) {
                var row = containerMultimedia.getChildAt(containerMultimedia.childCount - 1) as LinearLayout
                var imageView = ImageView(applicationContext)
                imageView.setOnClickListener{ openMultimediaActivity() }
                if (row.childCount == 2) {
                    row = LinearLayout(applicationContext)
                    row.layoutParams = paramsRow
                    row.orientation = LinearLayout.HORIZONTAL
                    containerMultimedia.addView(row)
                }

                imageView.setPadding(5,5,5,5)
                imageView.layoutParams = paramsImage

                if (selectedCard.multimedia[i].contains("video")) { // If media is a video, setThumbnail to imageView
                    // Do Nothing
                } else if (selectedCard.multimedia[i].contains("image")) { // else, set image
                    Picasso.get().load(selectedCard.multimedia[i]).centerCrop().fit().into(imageView)
                }
                row.addView(imageView)
            }
        }
    }

    fun openMultimediaActivity() {
        val intent = Intent(this, MultimediaActivity::class.java)
        intent.putStringArrayListExtra("multimedia", selectedCard.multimedia as ArrayList<String>)
        startActivityForResult(intent, 1)
    }

    fun setComments(user: User) {
        var cardRecyclerView = findViewById(R.id.recycler_comments) as RecyclerView
        val publishComment = findViewById(R.id.publish_comment) as MaterialButton

        val mLayoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL,false)
        cardRecyclerView?.layoutManager = mLayoutManager
        var commentAdapter = CommentAdapter(selectedCard.comments as ArrayList<Comment>)
        cardRecyclerView?.adapter = commentAdapter

        publishComment.setOnClickListener(View.OnClickListener { addComment(user) })
    }

    fun setCategories() {
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

    fun setLink() {
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

    fun setSettingsButton() {
        if (FirebaseUtils.isCurrentUser(selectedCard.userObj.id)) {
            findViewById<MaterialButton>(R.id.settings_button).visibility = View.VISIBLE

            var settingsButton = findViewById(R.id.settings_button) as Button
            settingsButton.setOnClickListener(View.OnClickListener() {
                var popup = PopupMenu(this, settingsButton);
                popup.getMenuInflater().inflate(R.menu.settings_detailed_card, popup.getMenu())

                popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener() {
                    if (it.title == "Edit Card") {
                        editCard()
                        true
                    } else if (it.title == "Delete Card") {
                        deleteCard()
                        true
                    }
                    false
                })
                popup.show()
            })
        }
    }

    fun editCard() {
        val intent = Intent(applicationContext, EditCardActivity::class.java)
        intent.putExtra("card", selectedCard)
        applicationContext.startActivity(intent)
        initData()
    }

    fun deleteCard() {
        Log.e("[DELETE CARD]", "Missing Function")
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
    }

}
