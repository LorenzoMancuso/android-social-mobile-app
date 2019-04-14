package com.example.rathings

import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener



object FirebaseUtils {

    var userObservable:CustomObservable=CustomObservable()

    val database = FirebaseDatabase.getInstance().reference

    @JvmStatic fun basePost() {
        database.child("users").child("TEST").child("username").setValue("Utente di Test")
    }

    @JvmStatic fun createUserInstance(uid:String) {
        Log.d("[FIREBASE-UTILS]", "createUserInstance $uid")
        database.child("users").child(uid).child("id").setValue(uid)
    }

    @JvmStatic fun getProfile(uid:String) {
        val ref = FirebaseUtils.database.child("users")
        var user:User?
        val phoneQuery = ref.orderByChild("id").equalTo(uid)

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    user = singleSnapshot.getValue(User::class.java)
                    userObservable.setValue(user)
                    Log.e("[FIREBASE-UTILS]", "onDataChange " + user?.toString())
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("[FIREBASE-UTILS]", "onCancelled", databaseError.toException())
            }
        }
        phoneQuery.addValueEventListener(postListener)
    }

}