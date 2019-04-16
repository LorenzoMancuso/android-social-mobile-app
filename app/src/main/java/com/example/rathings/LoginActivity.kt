package com.example.rathings

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils

import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


import kotlinx.android.synthetic.main.activity_login.*

/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity() {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    //private var mAuthTask: UserLoginTask? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        email_sign_in_button.setOnClickListener { signIn(txt_email.text.toString(),txt_password.text.toString()) }
        email_sign_up_button.setOnClickListener { signUp(txt_email.text.toString(),txt_password.text.toString()) }
        google_sign_in_button.setOnClickListener { googleSignIn() }

        /**FIREBASE AUTH INIT*/
        FirebaseApp.initializeApp(this)
        Log.d(TAG, "FirebaseApp:initialized")
        auth = FirebaseAuth.getInstance()
        Log.d(TAG, "FirebaseAuth:initialized")

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            //text_signin_status.text = "User ID: $currentUser.uid"
            goToHome()
        }
    }

    private fun goToHome() {
        //go to login
        FirebaseUtils.getProfile(null)
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }

    private fun signIn(email: String, password: String) {
        Log.d(TAG, "signIn:$email")
        if (!validateForm()) {
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    goToHome()

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    text_signin_status.text = "Error: sign in failed"
                }
            }
    }

    private fun signUp(email: String, password: String) {
        Log.d(TAG, "createAccount:$email")
        if (!validateForm()) {
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    createUserInstance()
                    goToHome()
                    /*val intent = Intent(this, ModifyAccountActivity::class.java)
                    startActivity(intent)*/
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    text_signin_status.text = "Error: Authentication failed"
                }
            }
    }

    private fun googleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    createUserInstance()
                    goToHome()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed with Google.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun createUserInstance() {
        Log.d(TAG, "createUserInstance")

        FirebaseUtils.createUserInstance(auth.currentUser!!.uid)
        UserController.getProfile(auth.currentUser!!.uid)
    }

    private fun validateForm(): Boolean {
        var valid = true

        val email = txt_email.text.toString()
        if (TextUtils.isEmpty(email)) {
            txt_email.error = "Required."
            valid = false
        } else {
            txt_email.error = null
        }

        val password = txt_password.text.toString()
        if (TextUtils.isEmpty(password)) {
            txt_password.error = "Required."
            valid = false
        } else {
            txt_password.error = null
        }

        return valid
    }

    companion object {
        private const val TAG = "[SIGNIN]"
        private const val RC_SIGN_IN = 9001
    }
}
