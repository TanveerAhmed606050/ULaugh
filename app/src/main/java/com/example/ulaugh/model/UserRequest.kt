package com.example.ulaugh.model

data class UserRequest(
    var phone_no: String?,
    val email: String? = null,
    val full_name: String? = null,
    val user_name: String? = null,
    var firebase_id: String? = null,
    val profile_pic: String? = null
)