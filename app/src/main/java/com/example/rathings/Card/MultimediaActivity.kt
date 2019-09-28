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
import com.example.rathings.R
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.squareup.picasso.Picasso
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory


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

    val listOfVideoPlayers: ArrayList<ExoPlayer> = ArrayList()
    fun setMultimedia(multimedia: ArrayList<String>) {
        var params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
        params.gravity = Gravity.CENTER_HORIZONTAL
        var padding = (20 * resources.displayMetrics.density + 0.5f).toInt()

        var container_images = findViewById<LinearLayout>(R.id.container_images)
        var container_videos = findViewById<LinearLayout>(R.id.container_videos)

        for (i in multimedia.indices) {
            var newLinearLayout = LinearLayout(applicationContext)
            newLinearLayout.layoutParams = params
            newLinearLayout.orientation = LinearLayout.VERTICAL
            newLinearLayout.setPadding(0, 0, 0, padding)

            if (multimedia[i].contains("image")) {
                var imageView = ImageView(applicationContext)

                imageView.layoutParams = params
                imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                Picasso.get().load(multimedia[i]).into(imageView)
                newLinearLayout.addView(imageView)

                container_images.addView(newLinearLayout)
            } else {
                var playerView = PlayerView(applicationContext)
                val player = ExoPlayerFactory.newSimpleInstance(applicationContext,  DefaultTrackSelector())
                listOfVideoPlayers.add(player)
                playerView.player = player
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM)

                var mediaSource = ExtractorMediaSource.Factory(DefaultDataSourceFactory(applicationContext, "rathings")).createMediaSource(Uri.parse(multimedia[i]))
                player.prepare(mediaSource)
                newLinearLayout.addView(playerView)

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

    override fun onDestroy() {
        super.onDestroy()
        if (listOfVideoPlayers.size > 0) {
            for (player in listOfVideoPlayers) {
                player.release()
            }
        }
    }
}
