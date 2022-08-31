package com.example.ulaugh.utils

import com.example.ulaugh.model.UserRequest

sealed class NetworkResult<T>(val data: T? = null, val message: String? = null) {

    class Success<T>(userRequest: UserRequest? =null, data: T? = null) : NetworkResult<T>(data)
    class Error<T>(message: String?, data: T? = null) : NetworkResult<T>(data, message)
    class Loading<T> : NetworkResult<T>()

}