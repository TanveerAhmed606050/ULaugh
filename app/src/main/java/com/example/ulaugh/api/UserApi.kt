package com.example.ulaugh.api

import com.example.ulaugh.model.SocialRequest
import com.example.ulaugh.model.UserRequest
import com.example.ulaugh.model.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UserApi {
    @POST("Users/signUp")
    suspend fun signup(@Body userRequest: UserRequest) : Response<UserResponse>

    @POST("Users/login")
    suspend fun signin(@Body userRequest: UserRequest) : Response<UserResponse>

    @POST("/users/signin")
    suspend fun socialLogin(@Body socialRequest: SocialRequest) : Response<UserResponse>
}