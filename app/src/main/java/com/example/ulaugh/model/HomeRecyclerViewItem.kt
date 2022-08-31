package com.example.ulaugh.model

sealed class HomeRecyclerViewItem {

    class NewsFeed(
        val id: Int,
        val title: String
    ) : HomeRecyclerViewItem()

    class GoogleAds(
        val id: Int,
        val title: String,
        val thumbnail: String,
        val release_date: String
    ) : HomeRecyclerViewItem()

    class Friends(
        val id: Int,
        val name: String,
        val avatar: String,
        val movie_count: Int
    ) : HomeRecyclerViewItem()

}
