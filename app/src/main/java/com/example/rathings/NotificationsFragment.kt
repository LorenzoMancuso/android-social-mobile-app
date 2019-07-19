package com.example.rathings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rathings.Card.CardAdapter
import com.example.rathings.R
import com.example.rathings.User.User
import com.example.rathings.utils.CustomObservable
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_cards.*
import java.util.*
import kotlin.collections.ArrayList


class NotificationsFragment : Fragment(){
    private var mListener: OnFragmentInteractionListener? = null
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: RecyclerView.Adapter<*>? = null

    private var localNotification = ArrayList<Notification>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_notifications_list, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FirebaseUtils.getNotifications().addObserver(object: Observer {
            override fun update(o: Observable?, arg: Any?) {
                val notifications = (o as CustomObservable).getValue() as ArrayList<Notification>
                localNotification = notifications
                updateNotifications(notifications)
            }
        })
    }

    fun updateNotifications(notification: ArrayList<Notification>){

        Log.d("[NOTIFICATION-FRAGMENT]", "Update notifications")

        mRecyclerView = view?.findViewById(R.id.my_recycler_view)
        val mLayoutManager = LinearLayoutManager(super.getContext(), RecyclerView.VERTICAL, false)
        mRecyclerView?.layoutManager = mLayoutManager

        Log.d("[NOTIFICATION-FRAGMENT]", "${notification}")

        mAdapter = NotificationAdapter(notification)
        mRecyclerView?.adapter = mAdapter
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

        FirebaseUtils.getPrimaryProfile().addObserver(object: Observer {
            override fun update(o: Observable?, arg: Any?) {
                val user = (o as CustomObservable).getValue() as User

                for (notification in user.notifications)
                    notification.read = true

                FirebaseUtils.updateData(
                    "users/${user.id}/",
                    user.toMutableMap()
                )
            }
        })

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
