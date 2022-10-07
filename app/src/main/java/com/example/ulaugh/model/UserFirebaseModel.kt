package com.example.unifiedapp.models

data class UserFirebaseModel(
    val user_name: String? = "",
    var firebase_id: String? = "",
    var personal_user_id: String? = "",
    var user_email: String? = "",
    var status: String? = ""
)