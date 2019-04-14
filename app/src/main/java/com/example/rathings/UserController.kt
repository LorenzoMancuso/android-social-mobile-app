package com.example.rathings

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import java.util.*

object UserController: Observer {

    var localUserProfileObservable=FirebaseUtils.userProfileObservable

    init {
        //localUserProfileObservable.addObserver(this)
    }

    fun getProfile(uid:String){
        FirebaseUtils.getProfile(uid)
    }

    fun getProfile(){
        FirebaseUtils.getProfile(FirebaseAuth.getInstance().currentUser!!.uid)
    }

    override fun update(observableObj: Observable?, data: Any?) {
        when(observableObj) {
            localUserProfileObservable -> {
                val user=localUserProfileObservable.getValue()
                Log.d("[USER-CONTROLLER]", "observable " + user?.toString())
            }
            else -> Log.d("[USER-CONTROLLER]", "observable not recognized $data")
        }
    }
}