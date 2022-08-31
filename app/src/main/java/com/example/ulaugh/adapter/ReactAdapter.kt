package com.example.ulaugh.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ulaugh.R
import com.example.ulaugh.databinding.AdapterReactLayoutBinding

class ReactAdapter(var context: Context) :
    RecyclerView.Adapter<ReactAdapter.ViewHolder>() {
    private var _binding: AdapterReactLayoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        _binding =
            AdapterReactLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(binding: AdapterReactLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (position) {
            0 -> {
                binding.mostEmoji.text = getEmojiByUnicode(0x1F928)
                binding.reactName.text = context.getText(R .string.express)
            }
            1 -> {
                binding.mostEmoji.text = getEmojiByUnicode(0x1F60A)
                binding.reactName.text = context.getText(R .string.sus_rea)
            }
            2 -> {
                binding.mostEmoji.text = getEmojiByUnicode(0x1F602)
                binding.reactName.text = context.getText(R .string.haha)
            }
            3 -> {
                binding.mostEmoji.text = getEmojiByUnicode(0x1F970)
                binding.reactName.text = context.getText(R .string.love_rea)
            }
            4 -> {
                binding.mostEmoji.text = getEmojiByUnicode(0x1F631)
//                binding.reactName.text = context.getText(R .string.sus_rea)
            }
        }
//        val inbox = reactList[position]
    }
    fun getEmojiByUnicode(unicode: Int): String {
        return String(Character.toChars(unicode))
    }
    override fun getItemCount(): Int {
        return 5
    }
}