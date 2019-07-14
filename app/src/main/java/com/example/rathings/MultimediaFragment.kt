package com.example.rathings

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import android.widget.*

class MultimediaFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View {
        super.onCreateView(inflater, container, savedInstanceState)
        val swipeView = inflater.inflate(R.layout.multimedia_fragment, container, false)
        // val imageView = swipeView.findViewById(R.id.imageView) as ImageView
        val layout = swipeView.findViewById(R.id.layout) as LinearLayout

        val cardMedia = getArguments()?.getString("media")
        Log.d("[MULTIMEDIA-FRAG]", cardMedia)

        if (cardMedia!!.contains("image")) {
            var imageView = ImageView(context)
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            params.gravity = Gravity.CENTER_HORIZONTAL
            imageView.layoutParams = params
            Picasso.get().load(cardMedia).centerCrop().fit().into(imageView)
            layout.addView(imageView)
        } else {
            // TODO: Risolvere il problema di cache per cui il video si vede SOLO una volta nel caso pesi qualche mega
            var videoView = VideoView(context)
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            params.gravity = Gravity.CENTER_HORIZONTAL
            videoView.layoutParams = params

            // Start the MediaController
            val mediaController = MediaController(context)
            mediaController.setAnchorView(videoView)

            // Get the URL from String VideoURL
            videoView.setMediaController(mediaController)
            videoView.setVideoURI(Uri.parse(cardMedia))
            videoView.setOnPreparedListener { mp ->
                mp.isLooping = true
                Log.i("[VIDEOVIEW-LOOP]", "Duration = " + videoView.duration)
            }

            videoView.start()
            layout.addView(videoView)
        }
        return swipeView
    }

    companion object {
        internal fun newInstance(cardMedia: String): MultimediaFragment {
            val swipeFragment = MultimediaFragment()
            val bundle = Bundle()
            bundle.putString("media", cardMedia)
            swipeFragment.setArguments(bundle)
            return swipeFragment
        }
    }
}