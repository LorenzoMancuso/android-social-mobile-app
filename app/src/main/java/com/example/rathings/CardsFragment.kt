package com.example.rathings

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.util.*
import kotlin.collections.ArrayList


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [CardsFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [CardsFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class CardsFragment : Fragment(), Observer {

    private var listener: OnFragmentInteractionListener? = null
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: RecyclerView.Adapter<*>? = null

    var interestCardsObs = CardController.interestCardObs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
        interestCardsObs.addObserver(this)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cards, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Start process to Initialize Data
        FirebaseUtils.getInterestCards(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        interestCardsObs.deleteObserver(this)
    }

    fun updateCardFragment(cards:ArrayList<Card>){
        mRecyclerView = view?.findViewById(R.id.my_recycler_view)
        val mLayoutManager = LinearLayoutManager(
            super.getContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        mRecyclerView?.layoutManager = mLayoutManager
        mAdapter = CardAdapter(cards)
        mRecyclerView?.adapter = mAdapter
    }

    override fun update(observableObj: Observable?, data: Any?) {
        when(observableObj) {
            interestCardsObs -> {
                val value = interestCardsObs.getValue()
                if (value is List<*>) {
                    val cards: ArrayList<Card> = ArrayList(value.filterIsInstance<Card>())
                    updateCardFragment(cards)
                    Log.d("[CARD-FRAGMENT]", "observable interest cards " + cards.toString())
                }
            }
            else -> Log.d("[CARD-FRAGMENT]", "observable not recognized $data")
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onCardsFragmentInteraction(uri)
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
        fun onCardsFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CardsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CardsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
