package com.example.ulaugh.interfaces

import com.example.ulaugh.model.Notification

interface NotificationListener {
    fun onNotification(notification: Notification, message:String)
}