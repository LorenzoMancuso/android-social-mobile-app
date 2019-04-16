package com.example.rathings


import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity(),CardsFragment.OnFragmentInteractionListener,TabsFragment.OnFragmentInteractionListener,ProfileFragment.OnFragmentInteractionListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        /**LOGOUT INIT*/
        auth = FirebaseAuth.getInstance()
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        /**END LOGOUT INIT*/


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
                R.id.action_options -> {
                    signOut()
                }
            }
            return@setOnNavigationItemSelectedListener true
        }

    }

    private fun switchFragment(mode:String?){
        Log.e("[MODE]", "$mode")
        when(mode){
            "tabs"->{
                val fragment = CardsFragment()
                supportFragmentManager.beginTransaction().replace(R.id.container, fragment, "Tabs").commit()
                selectItem(R.id.action_tabs)
            }
            "profile"->{
                val fragment = ProfileFragment()
                supportFragmentManager.beginTransaction().replace(R.id.container, fragment, "Profile").commit()
                selectItem(R.id.action_profile)
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

    fun signOut() {
        //Firebase Sign Out
        auth.signOut()

        // Google revoke access
        googleSignInClient.revokeAccess().addOnCompleteListener(this) {
            Log.e("[SIGN OUT]", "googleRevokeAccess:success")
        }

        googleSignInClient.signOut().addOnCompleteListener(this) {
            Log.e("[SIGN OUT]", "googleSignOut:success")
        }

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

}
