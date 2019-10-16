package com.example.rathings.Tab

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.rathings.FirebaseUtils
import com.example.rathings.R
import com.example.rathings.User.User
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

    var flatPalette: ArrayList<String> = ArrayList(Arrays.asList("#1abc9c", "#16a085", "#2ecc71", "#27ae60", "#3498db", "#2980b9", "#f1c40f", "#f39c12", "#e67e22", "#d35400", "#e74c3c", "#c0392b", "#9b59b6", "#8e44ad"))

    var tabsObs = TabController.tabsObs
    var primaryUserProfileObs = FirebaseUtils.getPrimaryProfile()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        tabsObs.addObserver(this)
        primaryUserProfileObs.addObserver(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTabs()
    }

    override fun update(observableObj: Observable?, data: Any?) {
        when(observableObj) {
            tabsObs -> {
                initTabs()
            }
            primaryUserProfileObs -> {
                // After setTab function (remove or add Tab)
                TabController.getTabs()
            }
            else -> Log.d("[TABS-FRAGMENT]", "observable not recognized $data")
        }
    }

    fun initTabs() {
        val container = view?.findViewById(R.id.container) as LinearLayout

        // Clean ScrollView
        container.removeAllViews()

        val value = tabsObs.getValue()
        val userInterests = (primaryUserProfileObs.getValue() as User).interests
        Log.d("[TABS-FRAGMENT]", userInterests.toString())
        Log.d("[TABS-FRAGMENT]", value.toString())
        if (value is ArrayList<*>) {
            val tabs: ArrayList<Tab> = ArrayList(value.filterIsInstance<Tab>())
            Log.d("[TABS-FRAGMENT]", "observable TABS " + tabs.toString())
            for (i in 0 until tabs.size) {
                var linearLayout = LinearLayout(context)

                if (i != 0) {
                    // Get the last layout If childCount exists
                    linearLayout = container.getChildAt(container.childCount - 1) as LinearLayout
                }

                if (linearLayout.childCount == 0 || linearLayout.childCount == 2) {
                    // If last layout.childCount == 2 OR It's the first tab set new layout
                    linearLayout = LinearLayout(context)
                    var params : LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    linearLayout.layoutParams = params
                    linearLayout.orientation = LinearLayout.HORIZONTAL
                    linearLayout.setPadding(5,5,5,5)
                    container.addView(linearLayout)
                }

                var button = Button(context)
                var params : LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (100 * resources.displayMetrics.density + 0.5f).toInt(), 1F)
                button.layoutParams = params
                button.gravity = Gravity.CENTER
                button.setBackgroundColor(Color.parseColor("#eeecec"))
                button.setTextColor(Color.BLACK)
                button.text = tabs[i].value
                button.setTypeface(button.typeface, Typeface.ITALIC)

                var selectedTab = false
                for (j in 0 until userInterests.size) {
                    if(userInterests[j] == tabs[i].id.toInt()) {
                        selectedTab = true
                        button.setBackgroundColor(Color.parseColor(flatPalette[i]))
                        button.setTextColor(Color.parseColor("#EEECEC"))
                    }
                }

                button.setOnClickListener(View.OnClickListener { setTab(tabs[i], selectedTab) })
                linearLayout.addView(button)
            }
        }
    }

    fun setTab(tab: Tab, value: Boolean) {
        Log.d("[TABS-FRAGMENT]", "Clicked Tab ${tab} with User Value ${value}")
        val user = primaryUserProfileObs.getValue() as User
        if (!value) {
            user.interests.add(tab.id)
        } else {
            for (i in 0 until user.interests.size) {
                Log.e("[TABS-FRAGMENT]", user.interests[i].toString() + " " + tab.id)
                if (user.interests[i] == tab.id) {
                    val result = user.interests.remove(user.interests[i])
                    break
                }
            }
        }
        user.interests.sort()
        Log.e("[TABS-FRAGMENT]", "Rimozione ${tab.id}" + TabController.toMutableMapForUser(user.interests).toString())
        FirebaseUtils.setData(
            "users/${user.id}/interests/",
            TabController.toMutableMapForUser(user.interests)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        primaryUserProfileObs.deleteObserver(this)
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
