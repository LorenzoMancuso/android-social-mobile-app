package com.example.rathings.Card

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.content.Intent
import android.provider.MediaStore
import android.graphics.Bitmap
import android.widget.*
import java.io.IOException
import android.content.DialogInterface
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import com.google.firebase.storage.FirebaseStorage
import android.widget.Toast
import android.app.ProgressDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.InputType
import android.util.Log
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.rathings.*
import com.example.rathings.Tab.Tab
import com.example.rathings.Tab.TabsActivity
import com.example.rathings.User.User
import com.example.rathings.utils.CustomObservable
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.storage.UploadTask
import de.hdodenhof.circleimageview.CircleImageView
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
        val addCategories = findViewById(R.id.add_categories) as Button
        addCategories.setOnClickListener(View.OnClickListener { addCategories() })

        val publishBtn = findViewById(R.id.publish_card) as Button
        publishBtn.setOnClickListener(View.OnClickListener { publishCard() })

        val linkBtn = findViewById(R.id.link_btn) as Button
        linkBtn.setOnClickListener(View.OnClickListener { addLink() })

        var multimetiaBtn = findViewById(R.id.multimedia_btn) as Button
        multimetiaBtn.setOnClickListener(View.OnClickListener() {
            var popup = PopupMenu(this, multimetiaBtn);
            popup.getMenuInflater().inflate(R.menu.multimedia, popup.getMenu())

            popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener() {
                if (it.title == "Image") {
                    chooseFile("image", 1)
                    true
                } else if (it.title == "Video") {
                    chooseFile("video", 2)
                    true
                }
                false
            })
            popup.show()
        })

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

    override fun update(o: Observable?, arg: Any?) {
        when(o) {
            userObs -> {
                var user: User = userObs.getValue() as User
                card.id = user.id + "_" + card.timestamp.toString()
                card.user = user.id

                // Set User info
                (findViewById(R.id.user) as TextView).text = "${user!!.name} ${user!!.surname}"
                val profile_image = findViewById(R.id.profile_image) as CircleImageView
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
            var addedLinkLayout = findViewById(R.id.added_link) as LinearLayout
            addedLinkLayout.removeAllViews()
            arguments.putString("URL", taskEditText.text.toString())
            linkPreviewFragment.setArguments(arguments)
            fragmentTransaction.add(R.id.container_link, linkPreviewFragment)
            fragmentTransaction.commit()
            addedLinkLayout.requestFocus()
            addedLink = taskEditText.text.toString()
        })
        .setNegativeButton("Cancel", null)
        .create()
        dialog.show()
        return true
    }

    private fun chooseFile(type: String, requestCode: Int) {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems = arrayOf("Select ${type} from gallery", "Do ${type} with camera")
        pictureDialog.setItems(pictureDialogItems,
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    0 -> {
                        val intent = Intent()
                        intent.type = "${type}/*"
                        intent.action = Intent.ACTION_GET_CONTENT
                        startActivityForResult(Intent.createChooser(intent, "Select ${type}"), requestCode)
                    }
                    1 -> {
                        if (type == "image") {
                            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                                takePictureIntent.resolveActivity(packageManager)?.also {
                                    startActivityForResult(takePictureIntent, 3)
                                }
                            }
                        } else {
                            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { takePictureIntent ->
                                takePictureIntent.resolveActivity(packageManager)?.also {
                                    startActivityForResult(takePictureIntent, 4)
                                }
                            }
                        }
                    }
                }
            })
        pictureDialog.show()
    }

    var listOfVideoPlayers: ArrayList<ExoPlayer> = ArrayList()
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 5) { // CASE Add Tab
            Log.d("[EXTRAS]", data?.extras?.get("added_categories").toString())

            // Re-initialize lists
            listOfSelectedTabs = ArrayList()
            listOfTabsIds = ArrayList()

            listOfSelectedTabs = data?.extras?.get("added_categories") as ArrayList<Tab>

            var addedCategories = findViewById(R.id.added_categories) as ChipGroup

            addedCategories.setOnCheckedChangeListener(ChipGroup.OnCheckedChangeListener() {chipGroup, id ->
                Log.d("[CHIP-GROUP]", chipGroup.toString())
                Log.d("[CHIP-GROUP]", id.toString())
            })
            addedCategories.removeAllViews()
            for(tab in listOfSelectedTabs) {
                // Add id to publish card
                listOfTabsIds.add(tab.id.toInt())

                // View value
                var chip = Chip(this)
                chip.text = tab.value
                chip.chipBackgroundColor = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_enabled)), intArrayOf(Color.parseColor(tab.color)))
                chip.setTextColor(Color.WHITE)
                chip.setCloseIconEnabled(true)
                addedCategories.addView(chip)

                chip.setOnCloseIconClickListener(View.OnClickListener { v ->
                    addedCategories.removeView(v)
                    listOfTabsIds.remove(tab.id.toInt())
                    listOfSelectedTabs.remove(tab)
                })
            }
        } else { // CASE Add Multimedia
            if (data != null) {
                val context = getApplicationContext()
                val addedMultimediaLayout = findViewById(R.id.added_multimedia) as LinearLayout
                var row = addedMultimediaLayout.getChildAt(addedMultimediaLayout.childCount - 1) as LinearLayout
                val scale = resources.displayMetrics.density

                if (row.childCount == 2) {
                    row = LinearLayout(context)
                    var params : LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
                    row.layoutParams = params
                    row.orientation = LinearLayout.HORIZONTAL
                    addedMultimediaLayout.addView(row)
                }

                if (requestCode == 1 || requestCode == 3) { // PHOTO: 1 = select, 3 = do
                    var imageView = ImageView(context)
                    var params : LinearLayout.LayoutParams = LinearLayout.LayoutParams((150 * scale + 0.5f).toInt(), (150 * scale + 0.5f).toInt(), 1F)
                    imageView.setPadding(5,5,5,5)
                    imageView.layoutParams = params
                    try {
                        var filePath: Uri
                        var bitmap:Bitmap

                        if (requestCode == 3) {
                            bitmap = data.getExtras().get("data") as Bitmap
                            filePath = Uri.parse(MediaStore.Images.Media.insertImage(contentResolver, bitmap, "image", null))
                        } else {
                            filePath = data?.data
                        }

                        Glide.with(this).load(filePath)
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(imageView)

                        uploadFile(filePath, (card.id) + "_" + (listOfDownloadUri.size+1), row, imageView, "image")
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } else if (requestCode == 2 || requestCode == 4) { // VIDEO: 2 = select, 4 = do
                    try {
                        var filePath = data?.data

                        var playerView = PlayerView(applicationContext)
                        val player = ExoPlayerFactory.newSimpleInstance(applicationContext,  DefaultTrackSelector())
                        var mediaSource = ExtractorMediaSource.Factory(DefaultDataSourceFactory(applicationContext, "rathings")).createMediaSource(filePath)
                        var thumbnail = ImageView(applicationContext)

                        listOfVideoPlayers.add(player)
                        playerView.layoutParams = LinearLayout.LayoutParams((150 * scale + 0.5f).toInt(), (150 * scale + 0.5f).toInt(), 1F)
                        playerView.setPadding(5,5,5,5)
                        playerView.player = player
                        playerView.useController = false

                        thumbnail.setBackgroundColor(Color.parseColor("#90111111"))
                        thumbnail.setImageResource(R.drawable.ic_slow_motion_video_white_48dp)
                        thumbnail.scaleType = ImageView.ScaleType.CENTER_INSIDE

                        playerView.overlayFrameLayout.addView(thumbnail)
                        player.prepare(mediaSource)

                        uploadFile(filePath, (card.id) + "_" + (listOfDownloadUri.size+1), row, playerView, "video")
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun uploadFile(filePath: Uri, name: String, row: LinearLayout, view: View, type: String) {

        var storage = FirebaseStorage.getInstance()
        var storageReference = storage.getReference()

        val ref = storageReference.child("${type}/${name}")

        val context = getApplicationContext()
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Uploading...")
        progressDialog.show()

        ref.putFile(filePath)
        .addOnFailureListener { e ->
            progressDialog.dismiss()
            Toast.makeText(context, "Failed " + e.message, Toast.LENGTH_SHORT).show()
        }
        .addOnProgressListener { taskSnapshot ->
            if (taskSnapshot.totalByteCount > 3145728) { // If file is major than 3MB
                taskSnapshot.task.cancel()
            } else {
                val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot
                    .totalByteCount
                progressDialog.setMessage("Uploaded " + progress.toInt() + "%")
            }
        }
        .addOnCanceledListener {
            Toast.makeText(context, "The file is major than 3 megabytes", Toast.LENGTH_LONG).show()
            progressDialog.dismiss()
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
                progressDialog.dismiss()
                Toast.makeText(context, "Uploaded", Toast.LENGTH_SHORT).show()
            } else {
                // Handle failures
            }
        }

    }

    fun deleteMedia(uri: String, row: LinearLayout, view: View) {
        if (listOfDownloadUri.size > 0) {
            var taskEditText = EditText(this)
            taskEditText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
            var dialog = AlertDialog.Builder(this)
                .setTitle("Delete media")
                .setMessage("Do you want delete this media?")
                .setPositiveButton("Yes", DialogInterface.OnClickListener() { _, _ ->
                    listOfDownloadUri.remove(uri)
                    Log.d("[DELETE MEDIA]", uri)
                    row.removeView(view)
                })
                .setNegativeButton("No", null)
                .create()
            dialog.show()
        }
    }

    fun publishCard() {
        val context = getApplicationContext()
        val titleText = (findViewById(R.id.title_text) as EditText).text.toString()
        val descriptionText = (findViewById(R.id.desc_text) as EditText).text.toString()

        if ((titleText != "" || descriptionText != "") && listOfTabsIds.size != 0) {
            card.title = (findViewById(R.id.title_text) as EditText).text.toString()
            card.description = (findViewById(R.id.desc_text) as EditText).text.toString()
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
            Toast.makeText(context, "Card published.", Toast.LENGTH_SHORT).show()
            finish()

        } else {

            var errorMessage = "You can't publish new card because "
            var listOfErrors: ArrayList<Any> = ArrayList()

            if (descriptionText == "") listOfErrors.add("Description")
            if (listOfTabsIds.size == 0) listOfErrors.add("Tab")

            for (i in listOfErrors.indices) {
                if (i == 0) {
                    errorMessage += listOfErrors[i]
                } else if (i == listOfErrors.size-1) {
                    errorMessage += " and " + listOfErrors[i]
                }
            }

            if (listOfErrors.size > 1) {
                errorMessage += " are mandatory."
            } else {
                errorMessage += " is mandatory."
            }

            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        userObs.deleteObserver(this)
        if (listOfVideoPlayers.size > 0) {
            for (player in listOfVideoPlayers) {
                player.release()
            }
        }
    }
}
