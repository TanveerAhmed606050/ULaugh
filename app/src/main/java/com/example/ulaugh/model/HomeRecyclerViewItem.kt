package com.example.ulaugh.model

sealed class HomeRecyclerViewItem{

    data class NewsFeed(
        val firebase_id: String = "",
        val image_url: String = "",
        val description: String = "",
        val date_time: String,
        val user_name: String,
        val full_name: String,
        val tagsList: String,
        val reaction: String
    ) : HomeRecyclerViewItem()

    data class GoogleAds(
        val id: Int,
        val title: String,
        val thumbnail: String,
        val release_date: String
    ) : HomeRecyclerViewItem()

    data class SuggestList(val friends: ArrayList<SuggestFriends>):HomeRecyclerViewItem()
}

data class SuggestFriends(
    var phone_no: String? = null,
    val email: String? = null,
    val full_name: String? = null,
    val user_name: String? = null,
    var firebase_id: String? = null,
    val profile_pic: String? = null
) : HomeRecyclerViewItem()
