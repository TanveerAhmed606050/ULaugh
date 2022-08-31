package com.example.ulaugh.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.ulaugh.api.UserApi
import com.example.ulaugh.model.UserRequest
import com.example.ulaugh.model.UserResponse
import com.example.ulaugh.utils.NetworkResult
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

class UserRepository @Inject constructor(private val userAPI: UserApi) {

    private val _userResponseLiveData = MutableLiveData<NetworkResult<UserResponse>>()
    val userResponseLiveData: LiveData<NetworkResult<UserResponse>>
        get() = _userResponseLiveData

    suspend fun registerUser(userRequest: UserRequest) {
        _userResponseLiveData.postValue(NetworkResult.Loading())
        val response = userAPI.signup(userRequest)
        handleResponse(response)
    }

    suspend fun loginUser(userRequest: UserRequest) {
        _userResponseLiveData.postValue(NetworkResult.Loading())
        val response = userAPI.signin(userRequest)
        handleResponse(response)
    }

//    suspend fun socialLogin(socialRequest: SocialRequest) {
//        _userResponseLiveData.postValue(NetworkResult.Loading())
//        val response = userAPI.socialLogin(socialRequest)
//        handleResponse(response)
//    }

    private fun handleResponse(response: Response<UserResponse>) {
//        if (response.isSuccessful && response.body() != null) {
//            _userResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
//        } else if (response.errorBody() != null) {
//            val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
//            _userResponseLiveData.postValue(NetworkResult.Error(errorObj.getString("message")))
//        } else {
//            _userResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
//        }
    }
}