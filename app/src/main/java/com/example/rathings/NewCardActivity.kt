package com.example.rathings

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.content.Intent
import android.provider.MediaStore
import android.graphics.Bitmap
import android.widget.*
import java.io.IOException
import android.content.DialogInterface
import android.net.Uri
import android.support.v7.app.AlertDialog
import com.google.firebase.storage.FirebaseStorage
import android.widget.Toast
import android.app.ProgressDialog
import android.util.Log
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class NewCardActivity : AppCompatActivity() {

    var listOfDownloadUri: MutableList<String> = ArrayList()
    var card = Card()
    var user = FirebaseUtils.getLocalUser()

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
            Picasso.with(this).load(user!!.profile_image).into(profile_image)
        }

        // Set OnClickListeners for buttons
        val addCategories = findViewById(R.id.add_categories) as Button
        addCategories.setOnClickListener(View.OnClickListener { addCategories() })

        val publishBtn = findViewById(R.id.publish_card) as Button
        publishBtn.setOnClickListener(View.OnClickListener { publishCard() })

        val imageBtn = findViewById(R.id.image_btn) as Button
        imageBtn.setOnClickListener(View.OnClickListener { chooseFile("image", 1) })

        val videoBtn = findViewById(R.id.video_btn) as Button
        videoBtn.setOnClickListener(View.OnClickListener { chooseFile("video", 2) })
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
        } else { // CASE Add Multimedia
            if (data != null) {
                val context = getApplicationContext()
                val tableLayout = findViewById(R.id.container_multimedia) as TableLayout
                var tableRow = tableLayout.getChildAt(tableLayout.childCount - 1) as TableRow

                if (tableRow.childCount == 3) {
                    tableRow = TableRow(context)
                    tableLayout.addView(tableRow)
                }

                val scale = resources.displayMetrics.density
                val leftRight = (25 * scale + 0.5f).toInt()
                val topBottom = (10 * scale + 0.5f).toInt()

                tableRow.setPadding(leftRight,topBottom,leftRight,topBottom)

                if (requestCode == 1 || requestCode == 3) {
                    val imageView = ImageView(context)
                    try {
                        var filePath: Uri
                        var bitmap:Bitmap

                        // TODO: Risolvere problema 'filePath must not be null'
                        if (requestCode == 3) {
                            bitmap = data.getExtras().get("data") as Bitmap
                            filePath = Uri.parse(MediaStore.Images.Media.insertImage(contentResolver, bitmap, "image", null))
                        } else {
                            filePath = data?.data
                            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath) // Archive
                        }

                        val resized = Bitmap.createScaledBitmap(bitmap, 300, 300, true)
                        imageView.setImageBitmap(resized)
                        imageView.setPadding(5,5,5,5)
                        tableRow.addView(imageView)
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
                        tableRow.addView(videoView)
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

        card.title = (findViewById(R.id.title_text) as EditText).text.toString()
        card.description = (findViewById(R.id.desc_text) as EditText).text.toString()
        card.category = listOfTabsIds
        card.multimedia = listOfDownloadUri
        card.ratings_average = 0.0F
        card.ratings_count = 0

        //send hash map of card object for firebase update
        println(card.multimedia)
        println(card.toMutableMap())
        FirebaseUtils.updateData("cards/${card.id}/",card.toMutableMap())
        Toast.makeText(context, "Card published.", Toast.LENGTH_SHORT).show()
        finish()
    }
}
