package com.example.rathings

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toolbar
import android.content.Intent
import android.provider.MediaStore
import android.graphics.Bitmap
import android.widget.ImageView
import java.io.IOException


class NewCardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_card)

        /*val mToolbar = findViewById(R.id.toolbar) as Toolbar
        mToolbar.setTitle(getString(R.string.app_name))
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)

        mToolbar.setNavigationOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                finish()
            }
        })*/

        val imageBtn = findViewById(R.id.image_btn) as Button
        imageBtn.setOnClickListener(View.OnClickListener { chooseFile("image") })

        val videoBtn = findViewById(R.id.video_btn) as Button
        videoBtn.setOnClickListener(View.OnClickListener { chooseFile("video") })

        val audioBtn = findViewById(R.id.audio_btn) as Button
        audioBtn.setOnClickListener(View.OnClickListener { chooseFile("audio") })

    }

    private fun chooseFile(type: String) {
        val intent = Intent()
        intent.type = "${type}/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select ${type}"), 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val imageView = findViewById(R.id.image_view) as ImageView
        if (requestCode == 1 && data != null && data.data != null) {
            val filePath = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                imageView.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun publishCard(view: View) {
        val title = findViewById(R.id.title_text) as EditText
        val description = findViewById(R.id.desc_text) as EditText
        println(title.text)
        println(description.text)
        finish()
    }
}
