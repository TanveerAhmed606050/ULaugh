package com.example.ulaugh.interfaces

interface FollowFriendListener {
    fun onFollow(firebaseId: String, rejected: String)
}