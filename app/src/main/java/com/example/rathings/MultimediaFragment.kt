package com.example.rathings

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.squareup.picasso.Picasso

class MultimediaFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View {
        super.onCreateView(inflater, container, savedInstanceState)
        val swipeView = inflater.inflate(R.layout.multimedia_fragment, container, false)
        val imageView = swipeView.findViewById(R.id.imageView) as ImageView
        val cardImage = getArguments().getString("image")
        Log.d("[MULTIMEDIA-FRAG]", cardImage)
        Picasso.with(context).load(cardImage).into(imageView)
        return swipeView
    }

    companion object {
        internal fun newInstance(cardImage: String): MultimediaFragment {
            val swipeFragment = MultimediaFragment()
            val bundle = Bundle()
            bundle.putString("image", cardImage)
            swipeFragment.setArguments(bundle)
            return swipeFragment
        }
    }
}