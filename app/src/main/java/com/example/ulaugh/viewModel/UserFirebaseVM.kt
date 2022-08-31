package com.example.ulaugh.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ulaugh.model.UserRequest
import com.example.ulaugh.model.UserResponse
import com.example.ulaugh.repository.UserFirebaseRepository
import com.example.ulaugh.utils.NetworkResult
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.UserInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserFirebaseVM @Inject constructor(private val userFirebaseRepository: UserFirebaseRepository) : ViewModel(){

    val userResponseLiveData: LiveData<NetworkResult<UserRequest>>
        get() = userFirebaseRepository.userResponseLiveData

    fun socialLogin(socialRequest: UserRequest){
        viewModelScope.launch {
            userFirebaseRepository.loginWithGoogle(socialRequest)
        }
    }

    fun signWithPhoneAuthCredential(phoneAuthCredential: PhoneAuthCredential, userRequest: UserRequest){
        viewModelScope.launch {
            userFirebaseRepository.signWithCredentials(phoneAuthCredential, userRequest)
        }
    }


}