package com.example.rathings.User

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.rathings.FirebaseUtils
import com.example.rathings.HomeActivity
import com.example.rathings.R
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_modify_account.*
import kotlinx.android.synthetic.main.activity_modify_account.profile_image
import kotlinx.android.synthetic.main.activity_modify_account.txt_country
import kotlinx.android.synthetic.main.activity_modify_account.txt_name
import kotlinx.android.synthetic.main.activity_modify_account.txt_profession
import java.io.File
import java.io.IOException
import java.util.*

class ModifyAccountActivity : AppCompatActivity(), Observer {

    var localUserProfileObservable = FirebaseUtils.userProfileObservable
    var user: User = User()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_account)

        localUserProfileObservable.addObserver(this)

        var builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        //call for get profile info
        FirebaseUtils.getProfile(null)

        change_image.setOnClickListener { changeImage() }

        confirm_button.setOnClickListener { updateInfo() }
    }

    fun updateInfo() {

        val regexp:Regex = Regex("^([0-2][0-9]|(3)[0-1])(\\/)(((0)[0-9])|((1)[0-2]))(\\/)\\d{4}\$")

        if (txt_birthdate?.text.toString().matches(regexp)) {
            //update local user
            user.name = txt_name?.text.toString()
            user.surname = txt_surname?.text.toString()
            user.birth_date = txt_birthdate?.text.toString()
            user.city = txt_city?.text.toString()
            user.country = txt_country?.text.toString()
            user.profession = txt_profession?.text.toString()

            if (newProfileImageUri != "") {
                user.profile_image = newProfileImageUri
            }

            //send hash map of user object for firebase update
            FirebaseUtils.updateData("users/${user.id}/", user.toMutableMap())
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("mode", "profile")
            startActivity(intent)
        }else{
            txt_birthdate?.error = this.getString(R.string.date_of_birth_error)
        }

    }

    fun changeImage() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle(this.getString(R.string.change_image_dialog_title))
        val pictureDialogItems = arrayOf(this.getString(R.string.change_image_dialog_select), this.getString(R.string.change_image_dialog_do))
        pictureDialog.setItems(pictureDialogItems,
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    0 -> {
                        val intent = Intent()
                        intent.type = "image/*"
                        intent.action = Intent.ACTION_GET_CONTENT
                        startActivityForResult(Intent.createChooser(intent, "Select image"), 0)
                    }
                    1 -> {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                            // TODO: Use these methods to upload an image and not a thumbnail
                            //var photoFile = createImageFile()
                            //takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile))
                            takePictureIntent.resolveActivity(packageManager)?.also {
                                startActivityForResult(takePictureIntent, 1)
                            }
                        }
                    }
                }
            })
        pictureDialog.show()
    }

    fun createImageFile(): File {
        var timeStamp = System.currentTimeMillis()
        var imageFileName = "rathings_photo_$timeStamp"
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName,".jpg",storageDir)
        return image;
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            var filePath: Uri = Uri.parse("")
            var profileImageView = findViewById<ImageView>(R.id.profile_image)

            if (requestCode == 0) { // PHOTO: 1 = select, 3 = do
                try {
                    filePath = data?.data
                    Log.d("SELECT PHOTO", filePath.toString())
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                try {
                    var bitmap = data.getExtras().get("data") as Bitmap
                    filePath = Uri.parse(MediaStore.Images.Media.insertImage(contentResolver, bitmap, "image", null))
                    Log.d("MAKE A PHOTO PHOTO", filePath.toString())
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            uploadImage(filePath)
            Glide.with(this).load(filePath)
                .centerCrop().circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(profileImageView)
        }
    }

    var newProfileImageUri: String = ""
    private fun uploadImage(filePath: Uri) {

        var storage = FirebaseStorage.getInstance()
        var storageReference = storage.getReference()

        val ref = storageReference.child("profile_images/${user.id}_${(System.currentTimeMillis() / 1000).toInt()}")

        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle(this.getString(R.string.upload_media_dialog_title))
        progressDialog.show()

        ref.putFile(filePath)
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(applicationContext, this.getString(R.string.upload_toast_error), Toast.LENGTH_SHORT).show()
            }
            .addOnProgressListener { taskSnapshot ->
                if (taskSnapshot.totalByteCount > 3145728) { // If file is major than 3MB
                    taskSnapshot.task.cancel()
                } else {
                    val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot
                        .totalByteCount
                    progressDialog.setMessage(this.getString(R.string.upload_media_dialog_progress, progress.toInt()))
                }
            }
            .addOnCanceledListener {
                Toast.makeText(applicationContext, this.getString(R.string.upload_toast_error_file_size), Toast.LENGTH_LONG).show()
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
                    newProfileImageUri = task.result.toString()

                    progressDialog.dismiss()
                    Toast.makeText(applicationContext, this.getString(R.string.upload_toast_response), Toast.LENGTH_SHORT).show()
                } else {
                    // Handle failures
                }
            }
    }

    override fun update(observableObj: Observable?, data: Any?) {
        when(observableObj) {
            localUserProfileObservable -> {
                val value=localUserProfileObservable.getValue()
                if(value is User){
                    user = value
                    txt_name?.text = Editable.Factory.getInstance().newEditable(user.name)
                    txt_surname?.text = Editable.Factory.getInstance().newEditable(user.surname)
                    txt_birthdate?.text = Editable.Factory.getInstance().newEditable(user.birth_date)
                    txt_city?.text = Editable.Factory.getInstance().newEditable(user.city)
                    txt_country?.text = Editable.Factory.getInstance().newEditable(user.country)
                    txt_profession?.text = Editable.Factory.getInstance().newEditable(user.profession)

                    if (user.profile_image != "") {
                        Glide.with(this).load(user.profile_image)
                            .centerCrop().circleCrop()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(profile_image)
                    }

                    Log.d("[PROFILE-FRAGMENT]", "PROFILE observable $user")
                }
            }
            else -> Log.d("[USER-CONTROLLER]", "observable not recognized $data")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        localUserProfileObservable.deleteObserver(this)
    }
}
