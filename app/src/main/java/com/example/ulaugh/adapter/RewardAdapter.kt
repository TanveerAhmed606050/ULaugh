package com.example.ulaugh.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ulaugh.databinding.AdapterReactLayoutBinding
import com.example.ulaugh.databinding.AdapterRewardBinding
import com.example.ulaugh.model.InboxListModel

class RewardAdapter (var context: Context):
RecyclerView.Adapter<RewardAdapter.ViewHolder>() {
    private var _binding: AdapterRewardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        _binding = AdapterRewardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(binding: AdapterRewardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    }

    override fun getItemCount(): Int {
        return 3
    }

}