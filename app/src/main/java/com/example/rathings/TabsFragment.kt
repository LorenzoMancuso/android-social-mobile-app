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

        tabsObs.addObserver(this)
        userProfileObs.addObserver(this)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTabs()
    }

    override fun update(observableObj: Observable?, data: Any?) {
        when(observableObj) {
            tabsObs -> {
                initTabs()
            }
            userProfileObs -> {
                // After setTab function (remove or add Tab)
                FirebaseUtils.getTabs()
            }
            else -> Log.d("[TABS-FRAGMENT]", "observable not recognized $data")
        }
    }

    fun initTabs() {
        // TODO: Qui c'è un BUG Quando vai nelle Tabs, torni alla Home e poi vai di nuovo nelle Tabs si rompe
        val tableLayout = view?.findViewById(R.id.container) as TableLayout

        // Clean TableLayout
        tableLayout.removeAllViews()

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
                // Add new Row
                var tableRow = TableRow(context)
                // tableLayout.setColumnStretchable(tableLayout.childCount - 1, true)
                tableLayout.addView(tableRow, tableLayout.childCount)

                var button = Button(super.getContext())
                var selectedTab = false
                var buttonText = tabs[i].value
                for (j in 0 until userInterests.size) {
                    if(userInterests[j] == tabs[i].id.toInt()) {
                        selectedTab = true
                        buttonText += "(remove)"
                    }
                }
                button.setBackgroundColor(Color.argb(255, 255, Random().nextInt(256), Random().nextInt(256)))
                button.text = buttonText

                // Set image to imageButton
                // imageButton.setImageResource(R.drawable.abc_ic_star_half_black_16dp)
                button.setOnClickListener(View.OnClickListener { setTab(tabs[i], selectedTab) })
                tableRow.addView(button)
            }
        }
    }

    fun setTab(tab: Tab, value: Boolean) {
        Log.d("[TABS-FRAGMENT]", "Clicked Tab ${tab} with User Value ${value}")
        val user = userProfileObs.getValue() as User
        if (!value) {
            user.interests.add(tab.id.toInt())
        } else {
            for (i in 0 until user.interests.size) {
                Log.e("[TABS-FRAGMENT]", user.interests[i].toString() + " " + tab.id)
                if (user.interests[i] == tab.id.toInt()) {
                    val result = user.interests.remove(user.interests[i])
                    break
                }
            }
        }
        user.interests.sort()
        Log.e("[TABS-FRAGMENT]", "Rimozione ${tab.id}" + tab.toMutableMapForUser(user.interests).toString())
        FirebaseUtils.setData("users/${user.id}/interests/",tab.toMutableMapForUser(user.interests))
    }

    override fun onDestroy() {
        super.onDestroy()
        userProfileObs.deleteObserver(this)
        tabsObs.deleteObserver(this)
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
