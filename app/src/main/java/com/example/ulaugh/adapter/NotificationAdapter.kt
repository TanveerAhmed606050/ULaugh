package com.example.ulaugh.adapter

import android.content.Context
import android.graphics.Typeface
import android.graphics.fonts.Font
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ulaugh.R
import com.example.ulaugh.databinding.AdapterNotificationBinding
import com.example.ulaugh.interfaces.FollowFriendListener
import com.example.ulaugh.interfaces.NotificationListener
import com.example.ulaugh.model.Notification
import com.example.ulaugh.utils.Constants
import com.example.ulaugh.utils.Helper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class NotificationAdapter(
    private val notificationList: ArrayList<Notification>,
    val context: Context,
    val notificationListener: NotificationListener
) :
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {
    private var _binding: AdapterNotificationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        _binding =
            AdapterNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    inner class ViewHolder(binding: AdapterNotificationBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notificationList[position]
        Glide.with(context)
            .load(notification.senderImage)
            .centerCrop()
            .fitCenter()
            .thumbnail()
            .placeholder(R.drawable.user_logo)
            .into(binding.profileIv)

        if (notification.title == "Request") {
            binding.followBtn.visibility = View.VISIBLE
            binding.declineBtn.visibility = View.VISIBLE
        }
        if (notification.seen != null) {
            if (!notification.seen!!) {
                binding.titleTv.typeface = Typeface.DEFAULT_BOLD
                binding.descriptionTv.typeface = Typeface.DEFAULT_BOLD
                binding.timeTv.typeface = Typeface.DEFAULT_BOLD
                binding.followBtn.typeface = Typeface.DEFAULT_BOLD
                binding.declineBtn.typeface = Typeface.DEFAULT_BOLD
            } else {
                binding.titleTv.typeface = Typeface.DEFAULT
                binding.descriptionTv.typeface = Typeface.DEFAULT
                binding.timeTv.typeface = Typeface.DEFAULT
                binding.followBtn.typeface = Typeface.DEFAULT
                binding.declineBtn.typeface = Typeface.DEFAULT
            }
        }
        binding.titleTv.text = notification.title
        binding.descriptionTv.text = notification.description
        binding.timeTv.text = Helper.convertToLocal(notification.time)
        binding.followBtn.setOnClickListener {
            notificationListener.onNotification(notification, Constants.ACCEPT)
        }
        binding.declineBtn.setOnClickListener {
            notificationListener.onNotification(notification, Constants.REJECTED)
        }
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }
}