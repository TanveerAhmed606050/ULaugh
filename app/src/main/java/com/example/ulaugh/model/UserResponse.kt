package com.example.ulaugh.model

data class UserResponse(
    val data: LoginData,
    val message: String,
    val status: Boolean
)
data class LoginData(
    val a_code: String,
    val code: String,
    val created_at: String,
    val email: String,
    val email_code: String,
    val f_code: String,
    val fb_token: String,
    val g_token: String,
    val id: String,
    val image: String,
    val modified: String,
    val name: String,
    val password: String,
    val phone_number: String,
    val status: String,
    val user_type: String,
    val username: String

)