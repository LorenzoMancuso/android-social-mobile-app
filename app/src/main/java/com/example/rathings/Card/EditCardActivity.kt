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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
        txt_title.text = Editable.Factory.getInstance().newEditable(selectedCard.title)
        txt_description.text = Editable.Factory.getInstance().newEditable(selectedCard.description)

        initToolbar()
        initCategories()
        initLink()
        initMultimedia()

        Log.e("[EDIT-CARD]", selectedCard.toString())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
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
        val linkBtn = findViewById<Button>(R.id.link_btn)
        linkBtn.setOnClickListener(View.OnClickListener { addLink() })

        if (selectedCard.link != "") {
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            val arguments = Bundle()
            var linkPreviewFragment = LinkPreviewFragment()
            arguments.putString("URL", selectedCard.link)
            linkPreviewFragment.arguments = arguments
            fragmentTransaction.add(R.id.added_link, linkPreviewFragment)
            fragmentTransaction.commit()
        }
    }

    fun addLink(): Boolean {
        var taskEditText = EditText(this)
        taskEditText.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        var dialog = AlertDialog.Builder(this)
            .setTitle(this.getString(R.string.add_link_dialog_title))
            .setMessage(this.getString(R.string.add_link_dialog_message))
            .setView(taskEditText)
            .setPositiveButton(this.getString(R.string.add_link_dialog_positive_button), DialogInterface.OnClickListener() { _, _ ->
                Log.d("[DIALOG]", taskEditText.text.toString())

                val fragmentManager = supportFragmentManager
                val fragmentTransaction = fragmentManager.beginTransaction()
                val linkPreviewFragment = LinkPreviewFragment()
                val arguments = Bundle()
                var containerLink = findViewById<LinearLayout>(R.id.container_link)
                containerLink.removeAllViews()
                arguments.putString("URL", taskEditText.text.toString())
                linkPreviewFragment.arguments = arguments
                fragmentTransaction.add(R.id.container_link, linkPreviewFragment)
                fragmentTransaction.commit()
                selectedCard.link = taskEditText.text.toString()
            })
            .setNegativeButton(this.getString(R.string.dialog_negative_button), null)
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

            var addedCategories = findViewById<ChipGroup>(R.id.added_categories)
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
        val addedMultimedia = findViewById<LinearLayout>(R.id.added_multimedia)
        addedMultimedia.removeAllViews()

        var newLinearLayout = LinearLayout(applicationContext)
        newLinearLayout.orientation = LinearLayout.HORIZONTAL
        newLinearLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
        addedMultimedia.addView(newLinearLayout)

        var paramsRow : LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)

        for (i in selectedCard.multimedia.indices) {
            var row = addedMultimedia.getChildAt(addedMultimedia.childCount - 1) as LinearLayout
            if (row.childCount == 2) {
                row = LinearLayout(applicationContext)
                row.layoutParams = paramsRow
                row.orientation = LinearLayout.HORIZONTAL
                addedMultimedia.addView(row)
            }

            manageMedia(row, selectedCard.multimedia[i], i)
        }

    }

    fun deleteMedia(index: Int) {
        if (selectedCard.multimedia.size > 0) {
            var taskEditText = EditText(this)
            taskEditText.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            var dialog = AlertDialog.Builder(this)
                .setTitle(this.getString(R.string.delete_media_dialog_title))
                .setMessage(this.getString(R.string.delete_media_dialog_message))
                .setPositiveButton(this.getString(R.string.delete_media_dialog_positive_button), DialogInterface.OnClickListener() { _, _ ->
                    selectedCard.multimedia.removeAt(index)
                    Log.d("[DELETE MEDIA]", index.toString())
                    initMultimedia()
                })
                .setNegativeButton(this.getString(R.string.dialog_negative_button), null)
                .create()
            dialog.show()
        }
    }

    fun manageMedia(row: LinearLayout, imagePath: String, index: Int) {
        val scale = resources.displayMetrics.density

        var imageView = ImageView(applicationContext)
        imageView.setPadding(5,5,5,5)
        imageView.layoutParams = LinearLayout.LayoutParams((150 * scale + 0.5f).toInt(), (150 * scale + 0.5f).toInt(), 1F)
        imageView.setOnClickListener { deleteMedia(index) }

        Glide.with(this).load(imagePath)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)

        row.addView(imageView)
    }

    fun updateCard() {
        selectedCard.category = listOfTabsIds
        selectedCard.title = txt_title.text.toString()
        selectedCard.description = txt_description.text.toString()
        FirebaseUtils.updateData("cards/${selectedCard.id}/", selectedCard.toMutableMap())
        Toast.makeText(baseContext, this.getString(R.string.toast_edit_response), Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}
