package com.example.rathings.Card

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.app.AlertDialog
import android.text.Editable
import android.text.InputType
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_edit_card.*
import kotlinx.android.synthetic.main.activity_edit_card.add_categories
import kotlinx.android.synthetic.main.activity_modify_account.confirm_button
import kotlinx.android.synthetic.main.activity_new_card.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class EditCardActivity : AppCompatActivity(), LinkPreviewFragment.OnFragmentInteractionListener {

    var tabsObs = TabController.tabsObs
    var selectedCard: Card = Card()
    var listOfDownloadUri: MutableList<String> = ArrayList()

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
        } else {
            Log.e("[PHOTOS Request Code]", requestCode.toString())
            val addedMultimediaLayout = findViewById<LinearLayout>(R.id.added_multimedia)
            var row = addedMultimediaLayout.getChildAt(addedMultimediaLayout.childCount - 1) as LinearLayout
            val scale = resources.displayMetrics.density

            if (row.childCount == 2) {
                row = LinearLayout(this)
                var params : LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
                row.layoutParams = params
                row.orientation = LinearLayout.HORIZONTAL
                addedMultimediaLayout.addView(row)
            }

            if (requestCode == 1 || requestCode == 2 || requestCode == 3 || requestCode == 4) { // PHOTO: 1 = select, 3 = do --- VIDEO: 2 = select, 4 = do
                var imageView = ImageView(this)
                var params : LinearLayout.LayoutParams = LinearLayout.LayoutParams((150 * scale + 0.5f).toInt(), (150 * scale + 0.5f).toInt(), 1F)
                imageView.setPadding(5,5,5,5)
                imageView.layoutParams = params
                try {
                    var filePath: Uri
                    if (requestCode == 3) {
                        filePath = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", photoFile)
                    } else {
                        Log.d("[SELECT IMAGE]", data?.data.toString())
                        filePath = data?.data as Uri
                    }

                    Glide.with(this).load(filePath)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imageView)

                    var type = "image"
                    if (filePath.toString().contains("video")) {
                        type = "video"
                    }

                    uploadFile(filePath, (selectedCard.id) + "_" + (listOfDownloadUri.size+1), row, imageView, type)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
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

        var multimetiaBtn = findViewById<Button>(R.id.multimedia_btn)
        multimetiaBtn.setOnClickListener(View.OnClickListener() {
            var popup = PopupMenu(this, multimetiaBtn);
            popup.menuInflater.inflate(R.menu.multimedia, popup.menu)

            popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener() {
                if (it.title == this.getString(R.string.multimedia_image)) {
                    chooseFile("image", 1)
                    true
                } else if (it.title == this.getString(R.string.multimedia_video)) {
                    chooseFile("video", 2)
                    true
                }
                false
            })
            popup.show()
        })

    }

    private fun chooseFile(type: String, requestCode: Int) {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action")

        var textType: String
        var requestCode = 1
        if (type == "image") {
            textType = this.getString(R.string.multimedia_image)
        } else {
            requestCode = 2
            textType = this.getString(R.string.multimedia_video)
        }

        val pictureDialogItems = arrayOf(this.getString(R.string.select_media_text, textType), this.getString(R.string.do_media_text, textType))
        pictureDialog.setItems(pictureDialogItems,
            DialogInterface.OnClickListener { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent()
                        intent.type = "${type}/*"
                        intent.action = Intent.ACTION_GET_CONTENT
                        startActivityForResult(Intent.createChooser(intent, "Select $textType"), requestCode)
                    }
                    1 -> {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), requestCode)
                        } else {
                            if (type == "image") {
                                doImageCamera()
                            } else {
                                doVideoCamera()
                            }
                        }
                    }
                }
            })
        pictureDialog.show()
    }

    var photoFile: File = File("")
    fun doImageCamera() {
        var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                photoFile = try {
                    CardController.createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    Log.e("[PHOTOS]", "Errore durante l'inserimento della foto")
                    File("")
                }
                // Continue only if the File was successfully created
                photoFile.also {
                    val photoURI: Uri = FileProvider.getUriForFile(this,BuildConfig.APPLICATION_ID + ".provider", it)
                    Log.e("[PHOTOS]", photoURI.toString())
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, 3)
                }
            }
        }
    }

    fun doVideoCamera() {
        Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, 4)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED)) { doImageCamera() }
                return
            }
            2 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED)) { doVideoCamera() }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun uploadFile(filePath: Uri, name: String, row: LinearLayout, view: View, type: String) {

        var storage = FirebaseStorage.getInstance()
        var storageReference = storage.getReference()

        val ref = storageReference.child("${type}/${name}")

        val context = getApplicationContext()
        progressbar.visibility = View.VISIBLE
        ref.putFile(filePath)
        .addOnFailureListener {
            progressbar.visibility = View.GONE
            Toast.makeText(context, this.getString(R.string.upload_toast_error), Toast.LENGTH_SHORT).show()
        }
        .addOnProgressListener { taskSnapshot ->
            /*if (taskSnapshot.totalByteCount > 3145728) { // If file is major than 3MB
                taskSnapshot.task.cancel()
            } else {
                val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot
                    .totalByteCount
                //progressDialog.setMessage(this.getString(R.string.upload_media_dialog_progress) + " " + progress.toInt() + "%")
            }*/
        }
        .addOnCanceledListener {
            Toast.makeText(context, this.getString(R.string.upload_toast_error_file_size), Toast.LENGTH_LONG).show()
            progressbar.visibility = View.GONE
        }
        .continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation ref.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Add ImageView or PlayerView only if UPLOAD done
                row.addView(view)
                selectedCard.multimedia.add(task.result.toString())
                view.setOnClickListener { deleteMedia(selectedCard.multimedia.indexOf(task.result.toString())) }
                progressbar.visibility = View.GONE
                Toast.makeText(context, this.getString(R.string.upload_toast_response), Toast.LENGTH_SHORT).show()
            } else {
                // Handle failures
            }
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
        if (listOfTabsIds.size > 0) {
            selectedCard.category = listOfTabsIds
        }
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
