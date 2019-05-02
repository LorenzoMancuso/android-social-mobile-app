package com.example.rathings

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [TabsFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [TabsFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class TabsFragment : Fragment(), Observer {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null

    var tabsObs = FirebaseUtils.tabsObservable
    var userProfileObs = FirebaseUtils.userProfileObservable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        // TODO: Continuare da qui! Non stampa come si deve (1)
        tabsObs.addObserver(this)
        userProfileObs.addObserver(this)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FirebaseUtils.getTabs()
    }

    override fun update(observableObj: Observable?, data: Any?) {
        when(observableObj) {
            tabsObs -> {
                val tableLayout = getView()?.findViewById(R.id.container) as TableLayout

                // Clean TableLayout in case of UPDATE
                tableLayout.removeAllViews()

                // Add new Row
                val tableRow = TableRow(context)
                tableLayout.addView(tableRow)

                /*val scale = resources.displayMetrics.density
                val leftRight = (25 * scale + 0.5f).toInt()
                val topBottom = (10 * scale + 0.5f).toInt()
                tableRow.setPadding(leftRight,topBottom,leftRight,topBottom)*/

                val value = tabsObs.getValue()
                val userInterests = (userProfileObs.getValue() as User).interests
                Log.d("[TABS-FRAGMENT]", userInterests.toString())
                Log.d("[TABS-FRAGMENT]", value.toString())
                if (value is ArrayList<*>) {
                    val tabs: ArrayList<Tab> = ArrayList(value.filterIsInstance<Tab>())
                    Log.d("[TABS-FRAGMENT]", "observable TABS " + tabs.toString())
                    for (i in 0 until tabs.size) {
                        var button = Button(super.getContext())
                        var selectedTab = false
                        var buttonText = tabs[i].value
                        for (j in 0 until userInterests.size) {
                            if(userInterests[j] == tabs[i].id.toInt()) {
                                selectedTab = true
                                buttonText += "(remove)"
                            }
                        }
                        button.setBackgroundColor(Color.argb(255, Random().nextInt(256), Random().nextInt(256), Random().nextInt(256)))
                        button.text = buttonText
                        // TODO: Layout -> go to next line (but It doesn't work)
                        /*val buttonParams = TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.MATCH_PARENT, 1f
                        )
                        imageButton.layoutParams = buttonParams*/
                        // Set image to imageButton
                        // imageButton.setImageResource(R.drawable.abc_ic_star_half_black_16dp)
                        button.setOnClickListener(View.OnClickListener { setTab(tabs[i], selectedTab) })
                        tableRow.addView(button)
                    }
                }
            }
            userProfileObs -> {
                FirebaseUtils.getTabs()
            }
            else -> Log.d("[TABS-FRAGMENT]", "observable not recognized $data")
        }
    }

    fun setTab(tab: Tab, value: Boolean) {
        Log.d("[TABS-FRAGMENT]", "Clicked Tab ${tab} with User Value ${value}")
        // TODO: Situazione stabile, ora se si clicca su un bottone il value è true o false a seconda del fatto che sia o meno un 'interest'. Bisogna adesso aggiungerlo all'utente
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tabs, container, false)
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onTabsFragmentInteraction(uri)
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
        fun onTabsFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TabsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TabsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
