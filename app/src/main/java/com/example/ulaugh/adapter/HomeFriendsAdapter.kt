package com.example.ulaugh.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ulaugh.R
import com.example.ulaugh.databinding.ItemFriendslistBinding
import com.example.ulaugh.interfaces.OnClickListener
import com.example.ulaugh.model.HomeRecyclerViewItem

class HomeFriendsAdapter(
    val context: Context,
    private val userList: HomeRecyclerViewItem.SuggestList,
    private val clickListener: OnClickListener
) : RecyclerView.Adapter<HomeFriendsAdapter.ViewHolder>() {
    private var _binding: ItemFriendslistBinding? = null
    private val binding get() = _binding!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        _binding =
            ItemFriendslistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(binding: ItemFriendslistBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val suggestFriends = userList.friends[position]
        binding.nameTv.text = suggestFriends.full_name
        binding.statusTv.text = suggestFriends.user_name
        Glide.with(context)
            .load(suggestFriends.profile_pic)
            .centerCrop()
            .fitCenter()
            .thumbnail(0.3f)
            .placeholder(R.drawable.seokangjoon)
            .into(binding.photoIv)

        binding.addFriend.setOnClickListener {
            clickListener.onClick(suggestFriends)
            binding.addFriend.isClickable = false
        }
    }

    override fun getItemCount(): Int {
        return userList.friends.size
    }


}