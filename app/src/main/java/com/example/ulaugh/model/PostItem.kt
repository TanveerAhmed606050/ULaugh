package com.example.ulaugh.model

data class PostItem(
    var post_id:String = "",
    val firebase_id: String = "",
    val image_url: String = "",
    val description: String = "",
    val date_time: String = "",
    val user_name: String = "",
    val full_name: String = "",
    val tagsList: String = "",
    val profile_image:String = "",
    val _profile_pic: String = ""
)

