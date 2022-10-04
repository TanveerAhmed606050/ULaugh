package com.example.ulaugh.interfaces

import com.example.ulaugh.model.Emoji
import com.example.ulaugh.model.HomeRecyclerViewItem
import com.example.ulaugh.model.SuggestFriends

interface PostClickListener {
    fun onClick(post: Any, type:String, emotionList:List<Emoji>? = null)

}