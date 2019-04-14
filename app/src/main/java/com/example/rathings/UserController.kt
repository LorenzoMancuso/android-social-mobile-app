package com.example.rathings

import android.util.Log
import java.util.*

class UserController: Observer {
    var localUserObservable=FirebaseUtils.userObservable
    init {
        localUserObservable.addObserver(this)
    }

    override fun update(observableObj: Observable?, data: Any?) {
        var user=localUserObservable.getValue()
        Log.e("[USER-CONTROLLER]", "onDataChange " + user?.toString())

    }

    companion object {
        fun getProfile(uid:String){
            FirebaseUtils.getProfile(uid)
        }
    }

}