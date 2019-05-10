package com.example.rathings

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import java.util.*

class DetailedCardActivity : AppCompatActivity(), Observer {

    var tabsObs = FirebaseUtils.tabsObservable
    var selectedCard: Card = Card()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed_card)
        tabsObs.addObserver(this)

        initData()
    }



    override fun update(observableObj: Observable?, data: Any?) {
        when(observableObj) {
            tabsObs -> {
                initData()
            }
            else -> Log.d("[DETAILED-CARD]", "observable not recognized $data")
        }
    }

    private fun initData() {
        var cards: ArrayList<Card> = intent.getSerializableExtra("card") as ArrayList<Card>
        selectedCard = cards[0]

        // User Name
        (findViewById(R.id.user) as TextView).text = "${selectedCard.userObj.name} ${selectedCard.userObj.surname}"

        // Date
        val date = java.util.Date(selectedCard.timestamp.toLong() * 1000)
        (findViewById(R.id.date) as TextView).text = java.text.SimpleDateFormat("yyyy-MM-dd' - 'HH:mm:ss", Locale.ITALY).format(date)

        // Profile Image
        Log.e("[DETAILED-CARD]", selectedCard.userObj.profile_image)
        if(profile_image != null && selectedCard.userObj.profile_image != "") {
            Picasso.with(this).load(selectedCard.userObj.profile_image).into(profile_image)
        }

        // Categories
        var containerCategories = findViewById(R.id.container_categories) as LinearLayout
        containerCategories.removeAllViews()
        var tabs = tabsObs.getValue() as ArrayList<Tab>
        for (i in 0 until selectedCard.category.size) {
            for (j in 0 until tabs.size) {
                if (selectedCard.category[i] == tabs[j].id.toInt()) {
                    var textView = TextView(this)
                    textView.text = tabs[j].value
                    containerCategories.addView(textView)
                }
            }
        }

        // Title
        (findViewById(R.id.title) as TextView).text = selectedCard.title

        // Description
        (findViewById(R.id.description) as TextView).text = selectedCard.description

        // Multimedia
        var multimediaPagerAdapter = MultimediaPagerAdapter(getSupportFragmentManager());
        var viewPager = findViewById(R.id.multimedia_pager) as ViewPager
        multimediaPagerAdapter.NUM_ITEMS = selectedCard.multimedia.size
        multimediaPagerAdapter.ITEMS = selectedCard.multimedia
        Log.d("[DETAILED-CARD]", multimediaPagerAdapter.NUM_ITEMS.toString())
        Log.d("[DETAILED-CARD]", multimediaPagerAdapter.ITEMS.toString())
        viewPager.setAdapter(multimediaPagerAdapter)

        // Comments
        var cardRecyclerView = findViewById(R.id.recycler_comments) as RecyclerView
        var commentsTitle = findViewById(R.id.comments_title) as Button

        if (selectedCard.comments.size == 0) {
            commentsTitle.text = "Click here to add the first comment!"
        } else {
            commentsTitle.text = "Comments"
        }
        val mLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        cardRecyclerView?.layoutManager = mLayoutManager
        var commentAdapter = CommentAdapter(selectedCard.comments as ArrayList<Comment>)
        cardRecyclerView?.adapter = commentAdapter

        val publishComment = findViewById(R.id.publish_comment) as Button
        publishComment.setOnClickListener(View.OnClickListener { addComment(selectedCard.comments.size.toString(), selectedCard.id.toString(), (findViewById(R.id.add_comment) as EditText).text.toString()) })

        Log.d("[DETAILED-CARD]", "Card: " + cards[0])
    }

    fun addComment(idNewComment: String, idCard: String, text: String) {
        var newComment = Comment()
        newComment.id = idNewComment.toLong()
        newComment.userObj = FirebaseUtils.getLocalUser() as User
        newComment.user = newComment.userObj.id
        newComment.text = text
        newComment.timestamp = (System.currentTimeMillis() / 1000).toInt()
        Log.d("[ADD-COMMENT]", newComment.toString())
        FirebaseUtils.updateData("cards/${idCard}/comments/${idNewComment}",newComment.toMutableMap())
    }

    override fun onDestroy() {
        super.onDestroy()
        tabsObs.deleteObserver(this)
    }

}
