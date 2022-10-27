package com.example.ulaugh.model

data class Notification(
    val senderId: String = "",
    val receiverId: String = "",
    val type: String = "",
    var title: String = "",
    var description: String = "",
    val time: String = "",
    val senderImage:String = "",
    var notificationId:String = "",
    var seen :Boolean? = null,
    val senderName:String=""
)
