package com.example.rathings.User

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.rathings.Card.Card
import com.example.rathings.Card.CardAdapter
import com.example.rathings.FirebaseUtils
import com.example.rathings.LoginActivity
import com.example.rathings.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_profile.*
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ProfileFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class ProfileFragment : Fragment(), Observer {

    var localUserCardsObservable= FirebaseUtils.userCardsObservable

    private var cardRecyclerView: RecyclerView? = null
    private var cardAdapter: RecyclerView.Adapter<*>? = null

    private var listener: OnFragmentInteractionListener? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        /**CONSTRUCTOR INIT*/
        super.onCreate(savedInstanceState)
        arguments?.let {}

        /**OBSERVER INIT*/
        localUserCardsObservable.addObserver(this)

        /**LOGOUT INIT*/
        auth = FirebaseAuth.getInstance()
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this.activity as Activity, gso)
        /**END LOGOUT INIT*/

    }

    fun goToEdit(){
        val intent = Intent(this.context, ModifyAccountActivity::class.java)
        startActivity(intent)
    }

    fun signOut() {
        //Firebase Sign Out
        auth.signOut()

        // Google revoke access
        googleSignInClient.revokeAccess().addOnCompleteListener(this.activity as Activity) {
            Log.e("[SIGN OUT]", "googleRevokeAccess:success")
        }

        googleSignInClient.signOut().addOnCompleteListener(this.activity as Activity) {
            Log.e("[SIGN OUT]", "googleSignOut:success")
        }

        val intent = Intent(this.activity as Activity, LoginActivity::class.java)
        startActivity(intent)
    }

    fun goToFollowList(requestType: String, followList: MutableList<Any>) {
        val intent = Intent(context, FollowListActivity::class.java)
        intent.putExtra("requestType", requestType)
        intent.putExtra("followList", followList as ArrayList<String>)
        startActivity(intent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view?.findViewById<Button>(R.id.btn_edit)!!.setOnClickListener { goToEdit() }
        view?.findViewById<Button>(R.id.btn_signout)!!.setOnClickListener { signOut() }

        //call for get profile info
        val value=FirebaseUtils.primaryUserProfileObservable.getValue()
        if(value is User){
            val user= value
            txt_name?.text = this.getString(R.string.name_surname, user.name, user.surname)
            txt_profession?.text = user.profession
            txt_country?.text = this.getString(R.string.city_country, user.city, user.country)
            txt_followers?.text = this.getString(R.string.followers_size, user.followers.size)
            txt_followed?.text = this.getString(R.string.followed_size, user.followed.size)
            if(profile_image!=null && user.profile_image != "") {
                Glide.with(this).load(user.profile_image)
                    .centerCrop().circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(profile_image)
            } else {
                Glide.with(this).load(R.drawable.default_avatar)
                    .centerCrop().circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(profile_image)
            }

            view?.findViewById<TextView>(R.id.txt_followers)!!.setOnClickListener{ goToFollowList("Followers", user.followers) }
            view?.findViewById<TextView>(R.id.txt_followed)!!.setOnClickListener{ goToFollowList("Followed", user.followed) }

            Log.d("[PROFILE-FRAGMENT]", "PROFILE $user")

            //call for get card of current user
            FirebaseUtils.getUserCards(user.id)
        }


        user_cards_recycler_view.isNestedScrollingEnabled = false

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onProfileFragmentInteraction(uri)
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

    override fun update(observableObj: Observable?, data: Any?) {
        when(observableObj) {

            localUserCardsObservable -> {
                val value = localUserCardsObservable.getValue()
                if (value is List<*>) {
                    val cards: ArrayList<Card> = ArrayList(value.filterIsInstance<Card>())
                    Log.d("[PROFILE-FRAGMENT]", "CARDS observable $cards")

                    txt_post.text = this.getString(R.string.cards_size, cards.size)

                    val tmp = ArrayList(cards.filter { it.ratings_average > 0 })
                    val avg = tmp.map { card -> card.ratings_average }.average().toFloat()

                    txt_score.text = this.getString(R.string.profile_rathing, (Math.round((avg) * 10.0) / 10.0).toString())

                    cardRecyclerView = view?.findViewById(R.id.user_cards_recycler_view)
                    val mLayoutManager = LinearLayoutManager(super.getContext(),RecyclerView.VERTICAL,false)
                    cardRecyclerView?.layoutManager = mLayoutManager
                    cardAdapter = CardAdapter(cards)
                    cardRecyclerView?.adapter = cardAdapter
                }
            }
            else -> Log.d("[USER-CONTROLLER]", "observable not recognized $data")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        localUserCardsObservable.deleteObserver(this)
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
        fun onProfileFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
