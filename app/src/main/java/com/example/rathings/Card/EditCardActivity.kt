package com.example.rathings.Card

import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.text.Editable
import android.text.InputType
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.example.rathings.*
import com.example.rathings.Tab.Tab
import com.example.rathings.Tab.TabController
import com.example.rathings.Tab.TabsActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_edit_card.*
import kotlinx.android.synthetic.main.activity_modify_account.confirm_button
import java.util.ArrayList

class EditCardActivity : AppCompatActivity(), LinkPreviewFragment.OnFragmentInteractionListener {

    var tabsObs = TabController.tabsObs
    var selectedCard: Card = Card()

    override fun onFragmentInteraction(uri: Uri) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_card)

        confirm_button.setOnClickListener { updateCard() }
        add_categories.setOnClickListener { addCategories() }

        selectedCard = intent.getSerializableExtra("card") as Card

        // Title and Description
        txt_title.text = Editable.Factory.getInstance().newEditable("${selectedCard.title}")
        txt_description.text = Editable.Factory.getInstance().newEditable("${selectedCard.description}")

        initToolbar()
        initCategories()
        initLink()
        initMultimedia()

        Log.e("[EDIT-CARD]", selectedCard.toString())
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

    fun initToolbar() {
        val toolbar = findViewById<View>(R.id.toolbar) as androidx.appcompat.widget.Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    fun initLink() {
        val linkBtn = findViewById(R.id.link_btn) as Button
        linkBtn.setOnClickListener(View.OnClickListener { addLink() })

        if (selectedCard.link != "") {
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            val linkPreviewFragment = LinkPreviewFragment()
            val arguments = Bundle()
            var addedLinkLayout = findViewById(R.id.added_link) as LinearLayout
            addedLinkLayout.removeAllViews()
            arguments.putString("URL", selectedCard.link)
            linkPreviewFragment.setArguments(arguments)
            fragmentTransaction.add(R.id.added_link, linkPreviewFragment)
            fragmentTransaction.commit()
        }
    }

    fun addLink(): Boolean {
        var taskEditText = EditText(this)
        taskEditText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        var dialog = AlertDialog.Builder(this)
            .setTitle("Add Link")
            .setMessage("Write or paste here a link")
            .setView(taskEditText)
            .setPositiveButton("Add", DialogInterface.OnClickListener() { _, _ ->
                Log.d("[DIALOG]", taskEditText.text.toString())

                val fragmentManager = supportFragmentManager
                val fragmentTransaction = fragmentManager.beginTransaction()
                val linkPreviewFragment = LinkPreviewFragment()
                val arguments = Bundle()
                var containerLink = findViewById(R.id.container_link) as LinearLayout
                containerLink.removeAllViews()
                arguments.putString("URL", taskEditText.text.toString())
                linkPreviewFragment.setArguments(arguments)
                fragmentTransaction.add(R.id.container_link, linkPreviewFragment)
                fragmentTransaction.commit()
                selectedCard.link = taskEditText.text.toString()
            })
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
        return true
    }

    // Identifiers to Publish Card
    var listOfTabsIds: ArrayList<Int> = ArrayList()
    // Tabs List from TabsActivity
    var listOfSelectedTabs: ArrayList<Tab> = ArrayList()
    fun initCategories() {
        var addedCategories = findViewById<ChipGroup>(R.id.added_categories)
        var tabs = tabsObs.getValue() as ArrayList<Tab>
        for (i in 0 until selectedCard.category.size) {
            for (j in 0 until tabs.size) {
                if (selectedCard.category[i] == tabs[j].id) {
                    listOfSelectedTabs.add(tabs[j])

                    var chip = Chip(this)
                    chip.text = tabs[j].value
                    chip.chipBackgroundColor = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_enabled)), intArrayOf(Color.parseColor(tabs[j].color)))
                    chip.setTextColor(Color.WHITE)
                    chip.setCloseIconVisible(false)
                    addedCategories.addView(chip)

                    chip.setOnCloseIconClickListener(View.OnClickListener { v ->
                        addedCategories.removeView(v)
                        listOfSelectedTabs.remove(tabs[j])
                    })
                }
            }
        }
    }

    fun addCategories() {
        val intent = Intent(this, TabsActivity::class.java)
        intent.putExtra("list_of_selected_tabs", listOfSelectedTabs)
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data?.extras?.get("added_categories") != null) {
            Log.d("[EXTRAS]", data.extras?.get("added_categories").toString())
            // Re-initialize lists
            listOfSelectedTabs = ArrayList()
            listOfTabsIds = ArrayList()

            listOfSelectedTabs = data.extras?.get("added_categories") as ArrayList<Tab>

            var addedCategories = findViewById(R.id.added_categories) as ChipGroup
            addedCategories.removeAllViews()
            for(tab in listOfSelectedTabs) {
                // Add id to publish card
                listOfTabsIds.add(tab.id)

                var chip = Chip(this)
                chip.text = tab.value
                chip.chipBackgroundColor = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_enabled)), intArrayOf(Color.parseColor(tab.color)))
                chip.setTextColor(Color.WHITE)
                chip.setCloseIconVisible(false)
                addedCategories.addView(chip)

                chip.setOnCloseIconClickListener(View.OnClickListener { v ->
                    addedCategories.removeView(v)
                    listOfTabsIds.remove(tab.id)
                    listOfSelectedTabs.remove(tab)
                })

            }
        }
    }

    fun initMultimedia() {
        val scale = resources.displayMetrics.density
        val addedMultimedia = findViewById<LinearLayout>(R.id.added_multimedia)
        addedMultimedia.removeAllViews()

        var newLinearLayout = LinearLayout(applicationContext)
        newLinearLayout.orientation = LinearLayout.HORIZONTAL
        newLinearLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
        addedMultimedia.addView(newLinearLayout)

        var paramsRow : LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)

        if (selectedCard.multimedia.size == 1) {
            var row = addedMultimedia.getChildAt(addedMultimedia.childCount - 1) as LinearLayout

            var imageView = ImageView(applicationContext)
            imageView.setPadding(5,5,5,5)

            Picasso.get().load(selectedCard.multimedia[0]).resize((300 * scale + 0.5f).toInt(), (300 * scale + 0.5f).toInt()).onlyScaleDown().centerInside().into(imageView)
            row.addView(imageView)
        } else if (selectedCard.multimedia.size > 0) {

            for (i in selectedCard.multimedia.indices) {
                var row = addedMultimedia.getChildAt(addedMultimedia.childCount - 1) as LinearLayout
                // row.setOnClickListener{ openMultimediaActivity() }

                if (row.childCount == 2) {
                    row = LinearLayout(applicationContext)
                    row.layoutParams = paramsRow
                    row.orientation = LinearLayout.HORIZONTAL
                    addedMultimedia.addView(row)
                }

                if (selectedCard.multimedia[i].contains("video")) { // If media is a video, setThumbnail to imageView and user ExoPlayer with disabled controls
                    manageVideo(row, selectedCard.multimedia[i])
                } else if (selectedCard.multimedia[i].contains("image")) { // else, set image
                    manageImage(row, selectedCard.multimedia[i])
                }
            }
        }

    }

    fun deleteMedia(path: String) {
        Log.d("[DELETE MEDIA]", path)
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
        thumbnail.setOnClickListener { deleteMedia(videoPath) }

        playerView.overlayFrameLayout.addView(thumbnail)
        player.prepare(mediaSource)

        row.addView(playerView)
    }

    fun manageImage(row: LinearLayout, imagePath: String) {
        val scale = resources.displayMetrics.density

        var imageView = ImageView(applicationContext)
        imageView.setPadding(5,5,5,5)
        imageView.layoutParams = LinearLayout.LayoutParams((150 * scale + 0.5f).toInt(), (150 * scale + 0.5f).toInt(), 1F)
        imageView.setOnClickListener { deleteMedia(imagePath) }

        Picasso.get().load(imagePath).centerCrop().fit().into(imageView)
        row.addView(imageView)
    }

    fun updateCard() {
        selectedCard.category = listOfTabsIds
        selectedCard.title = txt_title.text.toString()
        selectedCard.description = txt_description.text.toString()
        FirebaseUtils.updateData("cards/${selectedCard.id}/", selectedCard.toMutableMap())
        Toast.makeText(baseContext, "Edit done.", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (listOfVideoPlayers.size > 0) {
            for (player in listOfVideoPlayers) {
                player.release()
            }
        }
    }

}
