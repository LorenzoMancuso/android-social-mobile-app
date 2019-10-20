package com.example.rathings


import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.util.Log
import com.example.rathings.Card.CardsFragment
import com.example.rathings.Card.CardsInterestFragment
import com.example.rathings.Card.CardsPopularFragment
import com.example.rathings.Tab.TabController
import com.example.rathings.Tab.TabsFragment
import com.example.rathings.User.ProfileFragment
import com.example.rathings.User.SearchFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity(), CardsFragment.OnFragmentInteractionListener,
    TabsFragment.OnFragmentInteractionListener,
    ProfileFragment.OnFragmentInteractionListener,
    CardsPopularFragment.OnFragmentInteractionListener, CardsInterestFragment.OnFragmentInteractionListener,
    NotificationsFragment.OnFragmentInteractionListener, SearchFragment.OnFragmentInteractionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        /**NOTIFICATION LISTENER INIT*/
        NotificationUtils.notificationListener(this)

        // Initialize Tabs
        TabController.getTabs()

        switchFragment(intent.getStringExtra("mode"))

        bottom_navigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_home -> {
                    val fragment = CardsFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.container, fragment, "Cards").commit()
                }
                R.id.action_tabs -> {
                    val fragment = TabsFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.container, fragment, "Tabs").commit()
                }
                R.id.action_profile -> {
                    val fragment = ProfileFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.container, fragment, "Profile").commit()
                }
                R.id.action_search -> {
                    val fragment = SearchFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.container, fragment, "Search").commit()
                }
                R.id.action_options -> {
                    val fragment = NotificationsFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.container, fragment, "notifications").commit()
                }
            }
            return@setOnNavigationItemSelectedListener true
        }
    }

    private fun switchFragment(mode:String?){
        Log.e("[MODE]", "$mode")
        when(mode){
            "tabs"->{
                val fragment = TabsFragment()
                supportFragmentManager.beginTransaction().replace(R.id.container, fragment, "Tabs").commit()
                selectItem(R.id.action_tabs)
            }
            "profile"->{
                val fragment = ProfileFragment()
                supportFragmentManager.beginTransaction().replace(R.id.container, fragment, "Profile").commit()
                selectItem(R.id.action_profile)
            }
            "options"->{
                val fragment = NotificationsFragment()
                supportFragmentManager.beginTransaction().replace(R.id.container, fragment, "Notifications").commit()
                selectItem(R.id.action_profile)
            }
            "search"->{
                val fragment = SearchFragment()
                supportFragmentManager.beginTransaction().replace(R.id.container, fragment, "Search").commit()
                selectItem(R.id.action_search)
            }
            else -> {
                val fragment = CardsFragment()
                supportFragmentManager.beginTransaction().replace(R.id.container, fragment, "Cards").commit()
                selectItem(R.id.action_home)
            }
        }
    }

    fun selectItem(action:Int){
        var mBtmView =  findViewById<BottomNavigationView>(R.id.bottom_navigation);
        mBtmView.menu.findItem(action).setChecked(true);
    }

    override fun onCardsFragmentInteraction(uri: Uri) {}
    override fun onTabsFragmentInteraction(uri: Uri) {}
    override fun onProfileFragmentInteraction(uri: Uri) {}
    override fun onSearchFragmentInteraction(uri: Uri) {}

}
