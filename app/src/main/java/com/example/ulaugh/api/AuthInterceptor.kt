package com.example.ulaugh.api

import com.example.ulaugh.utils.SharePref
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor() : Interceptor {

    @Inject
    lateinit var sharePref: SharePref

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()

//        val token = tokenManager.getToken()
//        request.addHeader("Authorization", "Bearer $token")
        return chain.proceed(request.build())
    }
}