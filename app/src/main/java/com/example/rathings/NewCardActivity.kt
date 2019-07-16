package com.example.rathings

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
import android.graphics.Color
import android.text.InputType
import android.util.Log
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.card.*


class NewCardActivity : AppCompatActivity(),LinkPreviewFragment.OnFragmentInteractionListener {

    var listOfDownloadUri: MutableList<String> = ArrayList()
    var card = Card()
    var user = FirebaseUtils.getLocalUser()

    override fun onFragmentInteraction(uri: Uri) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_card)

        // Set Card info
        card.timestamp = (System.currentTimeMillis() / 1000).toInt()
        card.id = user?.id + "_" + card.timestamp.toString()
        card.user = user!!.id

        // Set User info
        (findViewById(R.id.user) as TextView).text = "${user!!.name} ${user!!.surname}"
        val profile_image = findViewById(R.id.profile_image) as CircleImageView
        if(user!!.profile_image != "") {
            Picasso.get().load(user!!.profile_image).into(profile_image)
        }

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

        /*val richLinkView = findViewById(R.id.richLinkView) as RichLinkViewTwitter
        richLinkView.setLink("https://google.com", object : ViewListener {
            override fun onSuccess(status: Boolean) {
            }
            override fun onError(e: Exception) {
            }
        })*/

    }

    // Identifiers to Publish Card
    var listOfTabsIds: ArrayList<Int> = ArrayList()
    // Tabs List from TabsActivity
    var listOfSelectedTabs: ArrayList<Tab> = ArrayList()
    fun addCategories() {
        val intent = Intent(this, TabsActivity::class.java)
        intent.putExtra("list_of_selected_tabs", listOfSelectedTabs)
        startActivityForResult(intent, 5)
    }

    var addedLink = ""
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
                var addedLinkLayout = findViewById(R.id.added_link) as LinearLayout
                addedLinkLayout.removeAllViews()
                arguments.putString("URL", taskEditText.text.toString())
                linkPreviewFragment.setArguments(arguments)
                fragmentTransaction.add(R.id.container_link, linkPreviewFragment)
                fragmentTransaction.commit()
                addedLinkLayout.requestFocus()
                addedLink = taskEditText.text.toString()
            }
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
                            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            startActivityForResult(Intent.createChooser(intent, "Do ${type}"), 3)
                        } else {
                            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                            startActivityForResult(Intent.createChooser(intent, "Do ${type}"), 4)
                        }
                    }
                }
            })
        pictureDialog.show()
    }

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

                if (requestCode == 1 || requestCode == 3) {
                    var imageView = ImageView(context)
                    var params : LinearLayout.LayoutParams = LinearLayout.LayoutParams((150 * scale + 0.5f).toInt(), (150 * scale + 0.5f).toInt(), 1F)
                    imageView.setPadding(5,5,5,5)
                    imageView.layoutParams = params
                    try {
                        var filePath: Uri
                        var bitmap:Bitmap

                        // TODO: Risolvere problema 'filePath must not be null'
                        if (requestCode == 3) {
                            bitmap = data.getExtras().get("data") as Bitmap
                            filePath = Uri.parse(MediaStore.Images.Media.insertImage(contentResolver, bitmap, "image", null))
                        } else {
                            filePath = data?.data
                        }

                        Picasso.get().load(filePath).centerCrop().fit().into(imageView)
                        row.addView(imageView)
                        uploadFile(filePath, (card.id) + "_" + (listOfDownloadUri.size+1), "image")
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } else if (requestCode == 2 || requestCode == 4) {
                    // TODO: Risolvere il problema di visualizzazione!
                    val videoView = VideoView(context)
                    try {
                        var filePath = data?.data
                        videoView.setVideoURI(filePath)
                        videoView.setPadding(5,5,5,5)
                        row.addView(videoView)
                        uploadFile(filePath, (card.id) + "_" + (listOfDownloadUri.size+1), "video")
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun uploadFile(filePath: Uri, name: String, type: String) {

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
            val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot
                .totalByteCount
            progressDialog.setMessage("Uploaded " + progress.toInt() + "%")
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
                listOfDownloadUri.add(task.result.toString())
                progressDialog.dismiss()
                Toast.makeText(context, "Uploaded", Toast.LENGTH_SHORT).show()
            } else {
                // Handle failures
            }
        }

        // PROGRESS DIALOG
        /*
        val context = getApplicationContext()
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Uploading...")
        progressDialog.show()

        ref.putFile(filePath)
        .addOnSuccessListener {
            progressDialog.dismiss()
            Toast.makeText(context, "Uploaded", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
            progressDialog.dismiss()
            Toast.makeText(context, "Failed " + e.message, Toast.LENGTH_SHORT).show()
        }
        .addOnProgressListener { taskSnapshot ->
            val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot
                .totalByteCount
            progressDialog.setMessage("Uploaded " + progress.toInt() + "%")
        }
        */

    }

    fun publishCard() {
        val context = getApplicationContext()
        val titleText = (findViewById(R.id.title_text) as EditText).text.toString()
        val descriptionText = (findViewById(R.id.desc_text) as EditText).text.toString()
        if (titleText != "" && descriptionText != "" && listOfTabsIds.size != 0) {
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
            FirebaseUtils.updateData("cards/${card.id}/",card.toMutableMap())
            Toast.makeText(context, "Card published.", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            var errorMessage = "You can't publish new card because "
            var listOfErrors: ArrayList<Any> = ArrayList()

            if (titleText == "") listOfErrors.add("Title")
            if (descriptionText == "") listOfErrors.add("Description")
            if (listOfTabsIds.size == 0) listOfErrors.add("Tab")

            for (i in listOfErrors.indices) {
                if (i == 0) {
                    errorMessage += listOfErrors[i]
                } else if (i == listOfErrors.size-1) {
                    errorMessage += " and " + listOfErrors[i]
                } else {
                    errorMessage += ", " + listOfErrors[i]
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
}
