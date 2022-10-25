package com.example.ulaugh.model

data class Notification(
    val senderId: String,
    val receiverId: String,
    val type: String,
    val title: String,
    val description: String,
    val time: String,
    val senderImage:String
)
