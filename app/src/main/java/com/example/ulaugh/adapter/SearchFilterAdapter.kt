package com.example.ulaugh.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ulaugh.R
import com.example.ulaugh.interfaces.FollowFriendListener
import com.example.ulaugh.interfaces.addFriendListener
import com.example.ulaugh.model.Friend
import com.example.ulaugh.model.SuggestFriends
import com.mikhaellopez.circularimageview.CircularImageView
import java.util.*
import kotlin.collections.ArrayList

class SearchFilterAdapter(
    private var context: Context,
    private var userList: ArrayList<SuggestFriends>,
    private val followFriendListener: FollowFriendListener
) : RecyclerView.Adapter<SearchFilterAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchFilterAdapter.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.home_fragment_nearby_list_layout, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SearchFilterAdapter.MyViewHolder, position: Int) {
        val user = userList[position]
        holder.statusNameTv.text = user.user_name
        holder.nameTv.text = user.full_name
        Glide.with(context)
            .load(user.profile_pic)
            .centerCrop()
            .fitCenter()
            .thumbnail()
            .placeholder(R.drawable.user_logo)
            .into(holder.photoView)

        holder.followView.setOnClickListener {
            followFriendListener.onFollow(user.firebase_id!!)
            userList.remove(user)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    // method for filtering our recyclerview items.
    fun filterList(filterlist: ArrayList<SuggestFriends>) {
        // below line is to add our filtered
        // list in our course array list.
        userList = filterlist
        // below line is to notify our adapter
        // as change in recycler view data.
        notifyDataSetChanged()
    }


    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var nameTv: TextView = view.findViewById(R.id.name_tv)
        var statusNameTv: TextView = view.findViewById(R.id.status_tv)
        var photoView: CircularImageView = view.findViewById(R.id.user_photo)
        var followView: View = view.findViewById(R.id.follow_view)
    }

}