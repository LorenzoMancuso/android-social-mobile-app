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
        var params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
        var padding = (20 * resources.displayMetrics.density + 0.5f).toInt()

        var container_multimedia = findViewById<LinearLayout>(R.id.container_multimedia)
        for (i in multimedia.indices) {
            var newLinearLayout = LinearLayout(applicationContext)
            newLinearLayout.layoutParams = params
            newLinearLayout.orientation = LinearLayout.VERTICAL
            newLinearLayout.setPadding(0, 0, 0, padding)

            var imageView = ImageView(applicationContext)

            imageView.layoutParams = params
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            Picasso.get().load(multimedia[i]).into(imageView)
            newLinearLayout.addView(imageView)

            container_multimedia.addView(newLinearLayout)
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
