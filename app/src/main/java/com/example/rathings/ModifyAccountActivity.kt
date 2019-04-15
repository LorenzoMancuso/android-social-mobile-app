package com.example.rathings

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_modify_account.*
import java.util.*

class ModifyAccountActivity : AppCompatActivity(), Observer {

    var localUserProfileObservable=FirebaseUtils.userProfileObservable
    var user:User=User()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_account)

        localUserProfileObservable.addObserver(this)

        //call for get profile info
        FirebaseUtils.getProfile(null)

        confirm_button.setOnClickListener { updateInfo() }
    }

    fun updateInfo() {
        //update local user
        user.name = txt_name?.text.toString()
        user.surname = txt_surname?.text.toString()
        user.birth_date = txt_birthdate?.text as Int
        user.city = txt_city?.text.toString()
        user.country = txt_country?.text.toString()
        user.profession = txt_profession?.text.toString()

        //send hash map of user object for firebase update
        
    }

    override fun update(observableObj: Observable?, data: Any?) {
        when(observableObj) {
            localUserProfileObservable -> {
                val value=localUserProfileObservable.getValue()
                if(value is User){
                    user = value
                    txt_name?.text = "${user.name}"
                    txt_surname?.text = "${user.surname}"
                    txt_birthdate?.text = "${user.birth_date}"
                    txt_city?.text = "${user.city}"
                    txt_country?.text = "${user.country}"
                    txt_profession?.text = "${user.profession}"
                    Log.d("[PROFILE-FRAGMENT]", "PROFILE observable $user")
                }
            }
            else -> Log.d("[USER-CONTROLLER]", "observable not recognized $data")
        }
    }
}
