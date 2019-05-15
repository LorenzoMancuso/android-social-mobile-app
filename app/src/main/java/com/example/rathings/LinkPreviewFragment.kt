package com.example.rathings

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import io.github.ponnamkarthik.richlinkpreview.MetaData
import io.github.ponnamkarthik.richlinkpreview.ResponseListener
import io.github.ponnamkarthik.richlinkpreview.RichPreview


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [LinkPreviewFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [LinkPreviewFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class LinkPreviewFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var URL: String? = null
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            URL = it.getString("URL")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var data: MetaData

        val richPreview = RichPreview(object : ResponseListener {
            override fun onData(metaData: MetaData) {
                data = metaData
                if (metaData.imageurl != "") {
                    Picasso.get().load(metaData.imageurl).into(view?.findViewById(R.id.image) as ImageView)
                }
                (view?.findViewById(R.id.title) as TextView)?.text = if (metaData.title.length > 32) metaData.title.substring(0, 30) + "..." else metaData.title
                (view?.findViewById(R.id.description) as TextView)?.text = if (metaData.description.length > 32) metaData.description.substring(0, 55) + "..." else metaData.description
                (view?.findViewById(R.id.link) as TextView)?.text = if (metaData.url.length > 32) metaData.url.substring(0, 20) + "..." else metaData.url
            }
            override fun onError(e: Exception) {
                //handle error
            }
        })

        Log.d("[LINK-PREVIEW]", URL)
        richPreview.getPreview(URL)

        return inflater.inflate(R.layout.fragment_link_preview, container, false)
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LinkPreviewFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LinkPreviewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
