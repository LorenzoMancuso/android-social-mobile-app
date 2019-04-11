package com.example.rathings

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object FirebaseUtils {

    @JvmStatic fun basePost() {
        val database: DatabaseReference = FirebaseDatabase.getInstance().reference
        database.child("users").child("TEST").child("username").setValue("Utente di Test")
    }





}