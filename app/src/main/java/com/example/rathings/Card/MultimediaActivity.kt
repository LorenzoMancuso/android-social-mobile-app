package com.example.rathings.Card

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.MediaController
import android.widget.VideoView
import com.example.rathings.R
import com.squareup.picasso.Picasso

class MultimediaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multimedia)

        /**SET TOOLBAR OPTION*/
        val toolbar = findViewById<View>(R.id.toolbar) as androidx.appcompat.widget.Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        /**-----------------*/

        var multimedia = intent.getStringArrayListExtra("multimedia") as ArrayList<String>
        Log.d("[MULTIMEDIA ACTIVITY]", multimedia.toString())

        setMultimedia(multimedia)

    }

    fun setMultimedia(multimedia: ArrayList<String>) {
        var params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
        params.gravity = Gravity.CENTER_HORIZONTAL
        var padding = (20 * resources.displayMetrics.density + 0.5f).toInt()

        var container_images = findViewById<LinearLayout>(R.id.container_images)
        var container_videos = findViewById<LinearLayout>(R.id.container_videos)

        for (i in multimedia.indices) {
            if (multimedia[i].contains("image")) {
                var newLinearLayout = LinearLayout(applicationContext)
                newLinearLayout.layoutParams = params
                newLinearLayout.orientation = LinearLayout.VERTICAL
                newLinearLayout.setPadding(0, 0, 0, padding)

                var imageView = ImageView(applicationContext)

                imageView.layoutParams = params
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                Picasso.get().load(multimedia[i]).into(imageView)
                newLinearLayout.addView(imageView)

                container_images.addView(newLinearLayout)
            } else {
                var newLinearLayout = LinearLayout(applicationContext)
                newLinearLayout.layoutParams = params
                newLinearLayout.orientation = LinearLayout.VERTICAL
                newLinearLayout.setPadding(0, 0, 0, padding)

                // videoView.setVideoURI(Uri.parse(multimedia[i]))
                // newLinearLayout.addView(videoView)

                container_videos.addView(newLinearLayout)
            }
        }
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
}
