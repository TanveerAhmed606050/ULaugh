package com.example.ulaugh.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.ulaugh.model.UserRequest
import com.example.ulaugh.utils.Constants
import com.example.ulaugh.utils.NetworkResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import javax.inject.Inject


class UserFirebaseRepository @Inject constructor() {
    private val _userResponseLiveData = MutableLiveData<NetworkResult<UserRequest>>()
    val userResponseLiveData: LiveData<NetworkResult<UserRequest>>
        get() = _userResponseLiveData
    val authFirebase = Firebase.auth
    var databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance().getReference(Constants.USERS_REF)

    fun loginWithGoogle(socialRequest: UserRequest) {
        _userResponseLiveData.postValue(NetworkResult.Loading())
        val credential = GoogleAuthProvider.getCredential(socialRequest.firebase_id, null)
        authFirebase.signInWithCredential(credential)
            .addOnCompleteListener {

                setFirebaseData(socialRequest)
            }
    }

    fun signWithCredentials(credential: PhoneAuthCredential, userRequest: UserRequest) {
        _userResponseLiveData.postValue(NetworkResult.Loading())
        authFirebase.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = authFirebase.currentUser

                    databaseReference.child(currentUser!!.uid).addValueEventListener(object :
                        ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.value == null) {
                                databaseReference.child(currentUser.uid).setValue(userRequest)
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            databaseReference.child(currentUser.uid)
                                                .addValueEventListener(object : ValueEventListener {
                                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                        val email = dataSnapshot.child("email").value.toString()
                                                        val fullName = dataSnapshot.child("full_name").value.toString()
                                                        val userName = dataSnapshot.child("user_name").value.toString()
                                                        val phoneNo = dataSnapshot.child("phone_no").value.toString()
                                                        val firebaseId = currentUser.uid
                                                        val profilePic = dataSnapshot.child("profile_pic").value.toString()

                                                        val userData = UserRequest(phoneNo, email, fullName, userName, firebaseId, profilePic)
                                                        _userResponseLiveData.postValue(NetworkResult.Success(userData))
                                                    }
                                                    override fun onCancelled(databaseError: DatabaseError) {}
                                                })
                                        } else if (it.isCanceled)
                                            _userResponseLiveData.postValue(NetworkResult.Error("signInWithCredential Canceled"))
                                        else
                                            _userResponseLiveData.postValue(NetworkResult.Error(it.exception?.message))
                                    }
                            } else {
                                val email = dataSnapshot.child("email").value.toString()
                                val fullName = dataSnapshot.child("full_name").value.toString()
                                val userName = dataSnapshot.child("user_name").value.toString()
                                val phoneNo = dataSnapshot.child("phone_no").value.toString()
                                val firebaseId = currentUser.uid
                                val profilePic = dataSnapshot.child("profile_pic").value.toString()

                                val userData = UserRequest(phoneNo, email, fullName, userName, firebaseId, profilePic)
                                _userResponseLiveData.postValue(NetworkResult.Success(userData))
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            _userResponseLiveData.postValue(NetworkResult.Error("Failed ${databaseError.message}"))
                        }
                    })
                } else {
                    _userResponseLiveData.postValue(NetworkResult.Error("Failed ${task.exception?.message}"))
                }

            }
    }

    private fun setFirebaseData(userInfo: UserRequest) {
        val idToken = authFirebase.currentUser!!.uid
//            authFirebase.databaseReference.child(idToken)
        databaseReference.child(idToken).setValue(userInfo)
            .addOnCompleteListener {
                if (it.isSuccessful && it.result != null) {
                    _userResponseLiveData.postValue(NetworkResult.Success(userInfo))
                } else if (it.isCanceled)
                    _userResponseLiveData.postValue(NetworkResult.Error("signInWithCredential Canceled"))
                else
                    _userResponseLiveData.postValue(NetworkResult.Error(it.exception?.message))
            }

    }
}