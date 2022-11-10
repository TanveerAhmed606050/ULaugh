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
import com.example.ulaugh.databinding.ItemMainBinding
import com.example.ulaugh.interfaces.AddFriendListener
import com.example.ulaugh.model.HomeRecyclerViewItem
import com.example.ulaugh.model.Reactions
import com.example.ulaugh.utils.Constants
import com.google.gson.Gson
import com.makeramen.roundedimageview.RoundedImageView

class PostsAdapter internal constructor(
    private val mContext: Context,
    private val postItems: MutableList<HomeRecyclerViewItem.SharePostData>,
    private val mClickListener: AddFriendListener
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

    fun countReactions(reactionList: MutableList<Reactions>): List<Pair<String?, Int>> {
        val frequencies = reactionList.groupingBy { it.reaction_type }.eachCount()
        return frequencies.toList().sortedByDescending { (key, value) -> value }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val itemView = inflater.inflate(R.layout.adapter_image_item, parent, false)
        return ListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
//        val postItem = postItems[position]
        holder.setPostImage(postItems[position])
//        setEmotions(emotionsList, mContext, binding)

    }

    private fun setEmotions(emotionsList: List<Pair<String?, Int>>, context: Context, binding: ItemMainBinding) {
        var position = 1 //set half emotions
        for (emotion in emotionsList) {
//                    Log.d(TAG, "setEmotions: ${position}")
            when (position) {
                1 -> {
                    when (emotion.first) {
                        "happy" -> binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
                        "sad" -> binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                        "fear" -> binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                        "neutral" -> binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
                        "angry" -> binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
                        "surprise" -> binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                        "disgust" -> binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                    }
                    binding.reactIv1.visibility = View.VISIBLE
                }
                2 -> {
                    when (emotion.first) {
                        "happy" -> binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
                        "sad" -> binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                        "fear" -> binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                        "neutral" -> binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
                        "angry" -> binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
                        "surprise" -> binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                        "disgust" -> binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                    }
                    binding.reactIv2.visibility = View.VISIBLE
                }
                3 -> {
                    when (emotion.first) {
                        "happy" -> binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
                        "sad" -> binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                        "fear" -> binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                        "neutral" -> binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
                        "angry" -> binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
                        "surprise" -> binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                        "disgust" -> binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                    }
                    binding.reactIv3.visibility = View.VISIBLE
                }
                4 -> {
                    when (emotion.first) {
                        "happy" -> binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
                        "sad" -> binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                        "fear" -> binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                        "neutral" -> binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
                        "angry" -> binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
                        "surprise" -> binding.reactIv4.setImageDrawable(
                            context.getDrawable(
                                R.drawable.fear_ic
                            )
                        )
                        "disgust" -> binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                    }
                    binding.reactIv4.visibility = View.VISIBLE
                }
                5 -> {
                    when (emotion.first) {
                        "happy" -> binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
                        "sad" -> binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                        "fear" -> binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                        "neutral" -> binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
                        "angry" -> binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
                        "surprise" -> binding.reactIv5.setImageDrawable(
                            context.getDrawable(
                                R.drawable.fear_ic
                            )
                        )
                        "disgust" -> binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                    }
                    binding.reactIv5.visibility = View.VISIBLE
                }
                6 -> {
                    when (emotion.first) {
                        "happy" -> binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
                        "sad" -> binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                        "fear" -> binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                        "neutral" -> binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
                        "angry" -> binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
                        "surprise" -> binding.reactIv6.setImageDrawable(
                            context.getDrawable(
                                R.drawable.fear_ic
                            )
                        )
                        "disgust" -> binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                    }
                    binding.reactIv6.visibility = View.VISIBLE
                }
                7 -> {
                    when (emotion.first) {
                        "happy" -> binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
                        "sad" -> binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                        "fear" -> binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                        "neutral" -> binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
                        "angry" -> binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
                        "surprise" -> binding.reactIv7.setImageDrawable(
                            context.getDrawable(
                                R.drawable.fear_ic
                            )
                        )
                        "disgust" -> binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                    }
                    binding.reactIv7.visibility = View.VISIBLE
                }
            }
            position++
        }
    }

    override fun getItemCount(): Int {
        return postItems.size
    }


}