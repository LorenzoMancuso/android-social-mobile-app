package com.example.rathings.Card

import android.Manifest
import android.content.ActivityNotFoundException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.content.Intent
import android.provider.MediaStore
import android.graphics.Bitmap
import android.widget.*
import java.io.IOException
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import com.google.firebase.storage.FirebaseStorage
import android.widget.Toast
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Environment
import android.text.InputType
import android.util.Log
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.rathings.*
import com.example.rathings.Tab.Tab
import com.example.rathings.Tab.TabsActivity
import com.example.rathings.User.User
import com.example.rathings.utils.CustomObservable
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_new_card.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class NewCardActivity : AppCompatActivity(), LinkPreviewFragment.OnFragmentInteractionListener, Observer {

    var listOfDownloadUri: MutableList<String> = ArrayList()

    var card = Card()
    var userObs: CustomObservable = CustomObservable()

    // Identifiers to Publish Card
    var listOfTabsIds: ArrayList<Int> = ArrayList()

    // Tabs List from TabsActivity
    var listOfSelectedTabs: ArrayList<Tab> = ArrayList()
    var addedLink = ""

    override fun onFragmentInteraction(uri: Uri) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_card)

        userObs = FirebaseUtils.getPrimaryProfile()
        userObs.addObserver(this)

        /**SET TOOLBAR OPTION*/
        val toolbar = findViewById<View>(R.id.toolbar) as androidx.appcompat.widget.Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayShowTitleEnabled(false)
        /**-----------------*/

        // Set Card info
        card.timestamp = (System.currentTimeMillis() / 1000).toInt()

        // Set OnClickListeners for buttons
        val addCategories = findViewById<Button>(R.id.add_categories)
        addCategories.setOnClickListener(View.OnClickListener { addCategories() })

        val publishBtn = findViewById<Button>(R.id.publish_card)
        publishBtn.setOnClickListener(View.OnClickListener { publishCard() })

        val linkBtn = findViewById<Button>(R.id.link_btn)
        linkBtn.setOnClickListener(View.OnClickListener { addLink() })

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun update(o: Observable?, arg: Any?) {
        when(o) {
            userObs -> {
                var user: User = userObs.getValue() as User
                card.id = user.id + "_" + card.timestamp.toString()
                card.user = user.id

                // Set User info
                findViewById<TextView>(R.id.user).text = this.getString(R.string.name_surname, user.name, user.surname)
                val profile_image = findViewById<ImageView>(R.id.profile_image)
                if(user.profile_image != "") {
                    Glide.with(this).load(user.profile_image)
                        .centerCrop().circleCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(profile_image)
                }
            }
        }
    }


    fun addCategories() {
        val intent = Intent(this, TabsActivity::class.java)
        intent.putExtra("list_of_selected_tabs", listOfSelectedTabs)
        startActivityForResult(intent, 5)
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
            var addedLinkLayout = findViewById<LinearLayout>(R.id.added_link)
            addedLinkLayout.removeAllViews()
            arguments.putString("URL", taskEditText.text.toString())
            linkPreviewFragment.arguments = arguments
            fragmentTransaction.add(R.id.container_link, linkPreviewFragment)
            fragmentTransaction.commit()
            addedLinkLayout.requestFocus()
            addedLink = taskEditText.text.toString()
        })
        .setNegativeButton(this.getString(R.string.dialog_negative_button), null)
        .create()
        dialog.show()
        return true
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("[PHOTOS Request Code]", requestCode.toString())
        if (resultCode == 5) { // CASE Add Tab
            Log.d("[EXTRAS]", data?.extras?.get("added_categories").toString())

            // Re-initialize lists
            listOfSelectedTabs = ArrayList()
            listOfTabsIds = ArrayList()

            listOfSelectedTabs = data?.extras?.get("added_categories") as ArrayList<Tab>

            var addedCategories = findViewById<ChipGroup>(R.id.added_categories)

            addedCategories.setOnCheckedChangeListener(ChipGroup.OnCheckedChangeListener() {chipGroup, id ->
                Log.d("[CHIP-GROUP]", chipGroup.toString())
                Log.d("[CHIP-GROUP]", id.toString())
            })
            addedCategories.removeAllViews()
            for(tab in listOfSelectedTabs) {
                // Add id to publish card
                listOfTabsIds.add(tab.id)

                // View value
                var chip = Chip(this)
                chip.text = tab.value
                chip.chipBackgroundColor = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_enabled)), intArrayOf(Color.parseColor(tab.color)))
                chip.setTextColor(Color.WHITE)
                chip.setCloseIconEnabled(true)
                addedCategories.addView(chip)

                chip.setOnCloseIconClickListener(View.OnClickListener { v ->
                    addedCategories.removeView(v)
                    listOfTabsIds.remove(tab.id)
                    listOfSelectedTabs.remove(tab)
                })
            }
        } else { // CASE Add Multimedia
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

                    uploadFile(filePath, (card.id) + "_" + (listOfDownloadUri.size+1), row, imageView, type)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun uploadFile(filePath: Uri, name: String, row: LinearLayout, view: View, type: String) {

        var storage = FirebaseStorage.getInstance()
        var storageReference = storage.getReference()

        val ref = storageReference.child("${type}/${name}")

        val context = getApplicationContext()
        progressBar.visibility = View.VISIBLE
        ref.putFile(filePath)
        .addOnFailureListener { _ ->
            progressBar.visibility = View.GONE
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
            progressBar.visibility = View.GONE
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

                listOfDownloadUri.add(task.result.toString())
                view.setOnClickListener { deleteMedia(task.result.toString(), row, view) }
                progressBar.visibility = View.GONE
                Toast.makeText(context, this.getString(R.string.upload_toast_response), Toast.LENGTH_SHORT).show()
            } else {
                // Handle failures
            }
        }

    }

    fun deleteMedia(uri: String, row: LinearLayout, view: View) {
        if (listOfDownloadUri.size > 0) {
            var taskEditText = EditText(this)
            taskEditText.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            var dialog = AlertDialog.Builder(this)
                .setTitle(this.getString(R.string.delete_media_dialog_title))
                .setMessage(this.getString(R.string.delete_media_dialog_message))
                .setPositiveButton(this.getString(R.string.delete_media_dialog_positive_button), DialogInterface.OnClickListener() { _, _ ->
                    listOfDownloadUri.remove(uri)
                    Log.d("[DELETE MEDIA]", uri)
                    row.removeView(view)
                })
                .setNegativeButton(this.getString(R.string.dialog_negative_button), null)
                .create()
            dialog.show()
        }
    }

    fun publishCard() {
        val titleText = findViewById<EditText>(R.id.title_text).text.toString()
        val descriptionText = findViewById<EditText>(R.id.desc_text).text.toString()

        if ((titleText != "" || descriptionText != "") && listOfTabsIds.size != 0) {
            card.title = findViewById<EditText>(R.id.title_text).text.toString()
            card.description = findViewById<EditText>(R.id.desc_text).text.toString()
            card.category = listOfTabsIds
            card.multimedia = listOfDownloadUri
            card.ratings_average = 0.0F
            card.ratings_count = 0

            if (addedLink != "") {
                card.link = addedLink
            }

            //send hash map of card object for firebase update
            println(card.multimedia)
            println(card.toMutableMap())
            FirebaseUtils.updateData("cards/${card.id}/", card.toMutableMap())
            Toast.makeText(this, this.getString(R.string.new_card_toast_response), Toast.LENGTH_SHORT).show()
            finish()

        } else {
            Toast.makeText(this, getString(R.string.new_card_toast_error), Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        userObs.deleteObserver(this)
    }
}
