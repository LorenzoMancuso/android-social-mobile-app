package com.example.rathings.Card

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.text.Editable
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.*
import com.example.rathings.*
import com.example.rathings.Tab.Tab
import com.example.rathings.Tab.TabController
import com.example.rathings.Tab.TabsActivity
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

        val linkBtn = findViewById(R.id.link_btn) as Button
        linkBtn.setOnClickListener(View.OnClickListener { addLink() })

        selectedCard = intent.getSerializableExtra("card") as Card

        // Title and Description
        txt_title.text = Editable.Factory.getInstance().newEditable("${selectedCard.title}")
        txt_description.text = Editable.Factory.getInstance().newEditable("${selectedCard.description}")

        // Tabs
        val addCategories = findViewById(R.id.add_categories) as Button
        addCategories.setOnClickListener(View.OnClickListener { addCategories() })

        var addedCategories = findViewById(R.id.added_categories) as ChipGroup
        var tabs = tabsObs.getValue() as ArrayList<Tab>
        for (i in 0 until selectedCard.category.size) {
            for (j in 0 until tabs.size) {
                if (selectedCard.category[i] == tabs[j].id.toInt()) {
                    listOfSelectedTabs.add(tabs[j])

                    var chip = Chip(this)
                    chip.text = tabs[j].value
                    chip.setChipBackgroundColorResource(R.color.bluePrimary)
                    chip.setTextColor(Color.WHITE)
                    chip.setCloseIconEnabled(true)
                    addedCategories.addView(chip)

                    chip.setOnCloseIconClickListener(View.OnClickListener { v ->
                        addedCategories.removeView(v)
                        listOfSelectedTabs.remove(tabs[j])
                    })
                }
            }
        }

        // Link
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

        // Multimedia
        if (selectedCard.multimedia.size > 0) {
            val addedMultimediaLayout = findViewById(R.id.added_multimedia) as LinearLayout
            val scale = resources.displayMetrics.density

            for (link in selectedCard.multimedia) {
                var row = addedMultimediaLayout.getChildAt(addedMultimediaLayout.childCount - 1) as LinearLayout

                if (row.childCount == 2) {
                    row = LinearLayout(this)
                    var params : LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
                    row.layoutParams = params
                    row.orientation = LinearLayout.HORIZONTAL
                    addedMultimediaLayout.addView(row)
                }

                var imageView = ImageView(this)
                var params : LinearLayout.LayoutParams = LinearLayout.LayoutParams((150 * scale + 0.5f).toInt(), (150 * scale + 0.5f).toInt(), 1F)
                imageView.setPadding(5,5,5,5)
                imageView.layoutParams = params

                Picasso.get().load(link).centerCrop().fit().into(imageView)
                row.addView(imageView)
            }
        }

        Log.e("[EDIT-CARD]", selectedCard.toString())
    }

    // Identifiers to Publish Card
    var listOfTabsIds: ArrayList<Int> = ArrayList()
    // Tabs List from TabsActivity
    var listOfSelectedTabs: ArrayList<Tab> = ArrayList()
    fun addCategories() {
        val intent = Intent(this, TabsActivity::class.java)
        intent.putExtra("list_of_selected_tabs", listOfSelectedTabs)
        startActivityForResult(intent, 1)
    }

    fun addLink(): Boolean {
        var taskEditText = EditText(this)
        taskEditText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        var errorEditText = EditText(this)
        var dialog = AlertDialog.Builder(this)
            .setTitle("Add Link")
            .setMessage("Write or paste here a link")
            .setView(taskEditText)
            .setPositiveButton("Add", DialogInterface.OnClickListener() { dialog, which ->
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data?.extras?.get("added_categories") != null) {
            Log.d("[EXTRAS]", data?.extras?.get("added_categories").toString())

            // Re-initialize lists
            listOfSelectedTabs = ArrayList()
            listOfTabsIds = ArrayList()

            listOfSelectedTabs = data?.extras?.get("added_categories") as ArrayList<Tab>

            var addedCategories = findViewById(R.id.added_categories) as ChipGroup
            addedCategories.removeAllViews()
            for(tab in listOfSelectedTabs) {
                // Add id to publish card
                listOfTabsIds.add(tab.id.toInt())

                var chip = Chip(this)
                chip.text = tab.value
                chip.setChipBackgroundColorResource(R.color.bluePrimary)
                chip.setTextColor(Color.WHITE)
                chip.setCloseIconEnabled(true)
                addedCategories.addView(chip)

                chip.setOnCloseIconClickListener(View.OnClickListener { v ->
                    addedCategories.removeView(v)
                    listOfTabsIds.remove(tab.id.toInt())
                    listOfSelectedTabs.remove(tab)
                })

            }
        }
    }

    fun updateCard() {
        selectedCard.category = listOfTabsIds
        selectedCard.title = txt_title.text.toString()
        selectedCard.description = txt_description.text.toString()
        FirebaseUtils.updateData("cards/${selectedCard.id}/", selectedCard.toMutableMap())
        Toast.makeText(baseContext, "Edit done.", Toast.LENGTH_SHORT).show()
        finish()
    }
}
