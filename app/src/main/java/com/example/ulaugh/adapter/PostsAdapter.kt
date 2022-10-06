package com.example.ulaugh.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ulaugh.R
import com.example.ulaugh.controller.ReactDetailActivity
import com.example.ulaugh.interfaces.PostClickListener
import com.example.ulaugh.model.Emoji
import com.example.ulaugh.model.HomeRecyclerViewItem
import com.example.ulaugh.model.Reactions
import com.example.ulaugh.utils.Constants
import com.google.gson.Gson
import com.makeramen.roundedimageview.RoundedImageView

class PostsAdapter internal constructor(
    private val mContext: Context,
    private val postItems: MutableList<HomeRecyclerViewItem.SharePostData>,
    private val mClickListener: PostClickListener
) :
    RecyclerView.Adapter<PostsAdapter.ListViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(mContext)

    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val postImageView: RoundedImageView = itemView.findViewById(R.id.imagePost)

        fun setPostImage(postItem: HomeRecyclerViewItem.SharePostData) {
//            Glide
//                .with(context)
//                .load("http://health.mlbranch.com/assets/images/lIJxNw16D3BL.jpg")
//                .into(postImageView)
            val emotionsList = countReactions(postItem.reaction!!)

            Glide.with(mContext)
                .load(postItem.image_url)
                .centerCrop()
                .fitCenter()
                .thumbnail()
                .placeholder(R.drawable.seokangjoon)
                .into(postImageView)
//            Glide.with(context).load(postItem.image_url).into(postImageView)
//            postImageView.setImageResource(postItem.image_url)
            itemView.setOnClickListener {
                val intent = Intent(mContext, ReactDetailActivity::class.java)
                intent.putExtra(Constants.POST, Gson().toJson(postItem))
                intent.putExtra(Constants.EMOTIONS_DATA, Gson().toJson(emotionsList))
                mContext.startActivity(intent)
            }
        }
    }

    fun countReactions(reactionList: MutableList<Reactions>): List<Emoji> {
        val emojiCount = ArrayList<Emoji>()
        for (emotion in reactionList) {
            run {
                if (emojiCount.any { it.name == "neutral" } && emotion.reaction_type == "neutral") {
                    emojiCount.find { it.name == "neutral" }!!.count++
//                        )
                } else if (emotion.reaction_type == "neutral") {
                    val emoji = Emoji("neutral", 1)
                    emojiCount.add(emoji)
                } else if (emojiCount.any { it.name == "happy" } && emotion.reaction_type == "happy") {
                    emojiCount.find { it.name == "happy" }!!.count++
                } else if (emotion.reaction_type == "happy") {
                    val emoji = Emoji("happy", 1)
                    emojiCount.add(emoji)
                } else if (emojiCount.any { it.name == "sad" } && emotion.reaction_type == "sad") {
                    emojiCount.find { it.name == "sad" }!!.count++
                } else if (emotion.reaction_type == "sad") {
                    val emoji = Emoji("sad", 1)
                    emojiCount.add(emoji)
                } else if (emojiCount.any { it.name == "surprise" } && emotion.reaction_type == "surprise") {
                    emojiCount.find { it.name == "surprise" }!!.count++
                } else if (emotion.reaction_type == "surprise") {
                    val emoji = Emoji("surprise", 1)
                    emojiCount.add(emoji)
                } else if (emojiCount.any { it.name == "angry" } && emotion.reaction_type == "angry") {
                    emojiCount.find { it.name == "angry" }!!.count++
                } else if (emotion.reaction_type == "angry") {
                    val emoji = Emoji("angry", 1)
                    emojiCount.add(emoji)
                } else if (emojiCount.any { it.name == "fear" } && emotion.reaction_type == "fear") {
                    emojiCount.find { it.name == "fear" }!!.count++
                } else if (emotion.reaction_type == "fear") {
                    val emoji = Emoji("fear", 1)
                    emojiCount.add(emoji)
                } else if (emojiCount.any { it.name == "disgust" } && emotion.reaction_type == "disgust") {
                    emojiCount.find { it.name == "disgust" }!!.count++
                } else {
                    val emoji = Emoji("disgust", 1)
                    emojiCount.add(emoji)
                }
            }
        }

        return emojiCount.sortedByDescending { it.count }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val itemView = inflater.inflate(R.layout.adapter_image_item, parent, false)
        return ListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
//        val postItem = postItems[position]
        holder.setPostImage(postItems[position])

    }

    override fun getItemCount(): Int {
        return postItems.size
    }


}