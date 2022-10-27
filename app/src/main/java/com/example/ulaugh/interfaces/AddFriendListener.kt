package com.example.ulaugh.interfaces

interface AddFriendListener {
    fun onClick(post: Any, type:String, emotionList:List<Pair<String?, Int>> = emptyList())
}