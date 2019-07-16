package com.example.rathings.Card

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.rathings.R
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_cards.*


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
class CardsFragment : Fragment(){

    private var mListener: OnFragmentInteractionListener? = null
    private var childFragments: Array<Fragment> = arrayOf(
        CardsPopularFragment(),
        CardsInterestFragment()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}

        val childFragment: Fragment = childFragments[0]
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.container_card, childFragment).commit()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cards, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        upper_navigation.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val childFragment: Fragment = childFragments[tab.position]
                val transaction = childFragmentManager.beginTransaction()
                transaction.replace(R.id.container_card, childFragment).commit()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }


    override fun onDestroy() {
        super.onDestroy()
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
}
