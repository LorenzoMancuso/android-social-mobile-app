package com.example.rathings

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_edit_card.*
import kotlinx.android.synthetic.main.activity_modify_account.*
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

        var addedCategories = findViewById(R.id.added_categories) as LinearLayout
        var tabs = tabsObs.getValue() as ArrayList<Tab>
        for (i in 0 until selectedCard.category.size) {
            for (j in 0 until tabs.size) {
                if (selectedCard.category[i] == tabs[j].id.toInt()) {
                    listOfSelectedTabs.add(tabs[j])
                    var textView = TextView(this)
                    textView.text = tabs[j].value
                    addedCategories.addView(textView)
                }
            }
        }

        // Link
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

        // Multimedia
        if (selectedCard.multimedia.size > 0) {
            val containerMultimedia = findViewById(R.id.container_multimedia) as LinearLayout
            val scale = resources.displayMetrics.density

            for (link in selectedCard.multimedia) {
                var row = containerMultimedia.getChildAt(containerMultimedia.childCount - 1) as LinearLayout

                if (row.childCount == 2) {
                    row = LinearLayout(this)
                    var params : LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
                    row.layoutParams = params
                    row.orientation = LinearLayout.HORIZONTAL
                    containerMultimedia.addView(row)
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
                if (!taskEditText.text.contains("http://") && !taskEditText.text.contains("https://")) {
                    Log.e("[DIALOG]", "Malformed URL")
                    Toast.makeText(this, "Malformed URL. Try to add 'http://' in your link.", Toast.LENGTH_LONG).show()
                } else {
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
                }
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

            var addedCategories = findViewById(R.id.added_categories) as LinearLayout
            addedCategories.removeAllViews()
            for(tab in listOfSelectedTabs) {
                // Add id to publish card
                listOfTabsIds.add(tab.id.toInt())

                // View value
                var textView = TextView(this)
                textView.text = tab.value
                addedCategories.addView(textView)
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
