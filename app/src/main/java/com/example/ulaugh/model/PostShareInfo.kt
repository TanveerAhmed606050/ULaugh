package com.example.ulaugh.model

data class PostShareInfo(
    val firebase_id: String = "",
    val image_url: String = "",
    val description: String = "",
    val date_time: String,
    val user_name: String,
    val full_name: String,
    val tagsList: String,
    val reaction: String,
    val media_type: String,
    val profile_image: String,
    val is_profile_pic: Boolean = false
)

data class TagsInfo(val tags: String)