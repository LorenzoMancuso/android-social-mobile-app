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
import android.view.View
import android.widget.*
import com.example.rathings.*
import com.example.rathings.Tab.Tab
import com.example.rathings.Tab.TabController
import com.example.rathings.User.ProfileActivity
import com.example.rathings.User.User
import com.example.rathings.utils.CustomObservable
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.squareup.picasso.Picasso
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

        tabsObs.addObserver(this)
        cardsObs.addObserver(this)
        FirebaseUtils.getInterestCards(null)

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
            }
            else -> Log.d("[DETAILED-CARD]", "observable not recognized $data")
        }
    }

    private fun initData() {
        // User, Title, Description, Categories, Link, Multimedia and Comments
        setUser()
        (findViewById(R.id.title) as TextView).text = selectedCard.title
        (findViewById(R.id.description) as TextView).text = selectedCard.description
        setCategories()
        setLink()
        setMultimedia()
        setComments()

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

        // Edit Card
        if (FirebaseUtils.isCurrentUser(selectedCard.userObj.id)) {
            (findViewById(R.id.edit_card) as FloatingActionButton).visibility = View.VISIBLE
        }

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

    fun setComments() {
        var cardRecyclerView = findViewById(R.id.recycler_comments) as RecyclerView
        val publishComment = findViewById(R.id.publish_comment) as Button
        var commentsTitle = findViewById(R.id.comments_title) as TextView

        commentsTitle.setOnClickListener(View.OnClickListener { enableComments() })

        val mLayoutManager = LinearLayoutManager(
            this,
            RecyclerView.VERTICAL,
            false
        )
        cardRecyclerView?.layoutManager = mLayoutManager
        var commentAdapter =
            CommentAdapter(selectedCard.comments as ArrayList<Comment>)
        cardRecyclerView?.adapter = commentAdapter

        publishComment.setOnClickListener(View.OnClickListener { addComment((findViewById( R.id.add_comment) as EditText).text.toString()) })
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

    fun editCard(view: View) {
        val intent = Intent(view.context, EditCardActivity::class.java)
        intent.putExtra("card", selectedCard)
        view.context.startActivity(intent)
        initData()
    }

    fun enableComments() {
        var commentsTitle = findViewById(R.id.comments_title) as TextView
        var addComment = findViewById(R.id.add_comment) as EditText
        val publishComment = findViewById(R.id.publish_comment) as Button
        if (commentsTitle.text == "Comments") {
            commentsTitle.text = "Click here to add a comment"
            addComment.visibility = View.GONE
            publishComment.visibility = View.GONE
        } else {
            commentsTitle.text = "Comments"
            addComment.visibility = View.VISIBLE
            publishComment.visibility = View.VISIBLE
        }
    }

    fun addComment(text: String) {
        var newComment = Comment()
        newComment.id = selectedCard.comments.size.toLong()
        newComment.userObj = FirebaseUtils.getLocalUser() as User
        newComment.user = newComment.userObj.id
        newComment.text = text
        newComment.timestamp = (System.currentTimeMillis() / 1000).toInt()
        Log.d("[ADD-COMMENT]", newComment.toString())
        FirebaseUtils.updateData(
            "cards/${selectedCard.id}/comments/${newComment.id }",
            newComment.toMutableMap()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        tabsObs.deleteObserver(this)
        cardsObs.deleteObserver(this)
        userObs.deleteObserver(this)
    }

}
