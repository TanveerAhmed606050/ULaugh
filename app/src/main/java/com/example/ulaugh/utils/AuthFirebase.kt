package com.example.ulaugh.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import javax.inject.Inject

class AuthFirebase @Inject constructor(){
    var auth: FirebaseAuth = Firebase.auth
//    fun getAuth(): FirebaseAuth {
//        return auth
//    }
    var databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference(Constants.USERS_REF)
//    fun getReference():DatabaseReference{
//        return databaseReference
//    }
}