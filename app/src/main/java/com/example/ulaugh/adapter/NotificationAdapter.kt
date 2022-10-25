package com.example.ulaugh.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.ulaugh.R
import com.example.ulaugh.databinding.AdapterNotificationBinding
import com.example.ulaugh.model.Notification
import jp.wasabeef.glide.transformations.BlurTransformation

class NotificationAdapter(val notificationList: ArrayList<Notification>, val context: Context) :
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

        binding.titleTv.text = notification.title
        binding.descriptionTv.text = notification.description
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }
}