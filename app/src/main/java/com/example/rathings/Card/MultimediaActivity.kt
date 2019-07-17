package com.example.rathings.Card

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
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
        var paramsImage = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
        var padding = (10 * resources.displayMetrics.density + 0.5f).toInt()

        var container_multimedia = findViewById<LinearLayout>(R.id.container_multimedia)
        for (i in multimedia.indices) {
            var imageView = ImageView(applicationContext)
            imageView.setPadding(padding, padding, padding, padding)
            imageView.layoutParams = paramsImage
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            Picasso.get().load(multimedia[i]).into(imageView)
            container_multimedia.addView(imageView)
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
