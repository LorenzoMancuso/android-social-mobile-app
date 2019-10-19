package com.example.rathings

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils

import android.util.Log
import android.widget.Toast
import com.example.rathings.User.ModifyAccountActivity
import com.example.rathings.User.UserController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


import kotlinx.android.synthetic.main.activity_login.*
import java.util.regex.Pattern

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
                    // Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    Snackbar.make(login_form, this.getString(R.string.sign_in_failed, task.exception.toString().split(':')[1]), Snackbar.LENGTH_SHORT).show();
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
                    // goToHome()
                    val intent = Intent(this, ModifyAccountActivity::class.java)
                    intent.putExtra("signup", true)
                    startActivity(intent)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Snackbar.make(login_form, this.getString(R.string.sign_up_failed, task.exception.toString().split(':')[1]), Snackbar.LENGTH_SHORT).show();
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
                Snackbar.make(login_form, this.getString(R.string.sign_in_failed, e.toString().split(':')[1]), Snackbar.LENGTH_SHORT).show();

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
                    Snackbar.make(login_form, this.getString(R.string.sign_in_failed, task.exception.toString().split(':')[1]), Snackbar.LENGTH_SHORT).show();
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
        } else if (!isValidEmail(email)){
            txt_email.error = "Invalid email."
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

        val EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )
        fun isValidEmail(email: CharSequence?): Boolean {
            return email != null && EMAIL_PATTERN.matcher(email).matches()
        }
    }
}
