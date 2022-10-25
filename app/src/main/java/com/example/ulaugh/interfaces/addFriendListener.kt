package com.example.ulaugh.interfaces

interface addFriendListener {
    fun onClick(post: Any, type:String, emotionList:List<Pair<String?, Int>> = emptyList())
}