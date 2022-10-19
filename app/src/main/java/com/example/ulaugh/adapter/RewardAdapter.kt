package com.example.ulaugh.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ulaugh.R
import com.example.ulaugh.databinding.AdapterRewardBinding
import com.example.ulaugh.model.Emoji
import com.example.ulaugh.model.HomeRecyclerViewItem
import com.example.ulaugh.model.Reactions
import com.example.ulaugh.utils.Helper
import java.util.ArrayList

class RewardAdapter(
    var context: Context,
    private val postList: ArrayList<HomeRecyclerViewItem.SharePostData>
) :
    RecyclerView.Adapter<RewardAdapter.ViewHolder>() {
    private var _binding: AdapterRewardBinding? = null
    private val binding get() = _binding!!
    private val emojiCount = ArrayList<Emoji>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        _binding = AdapterRewardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(binding: AdapterRewardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userPost = postList[position]
        Glide.with(context)
            .load(userPost.image_url)
            .centerCrop()
            .fitCenter()
            .thumbnail()
            .placeholder(R.drawable.user_logo)
            .into(binding.userPhoto)
        binding.rewardDetail.text = userPost.description
        binding.reactCount.text = Helper.prettyCount(userPost.reaction!!.size)
        val emotionsList = countReactions(userPost.reaction!!)
        setEmotions(emotionsList, context)
        if (userPost.reaction!!.size < 1000) {
            binding.claimBtn.background = context.getDrawable(R.drawable.grey_rc)
            binding.claimBtn.isEnabled = false
        }
    }

    private fun setEmotions(emotionsList: List<Emoji>, context: Context) {
        var position = 1 //set half emotions
        for (emotion in emotionsList) {
            when (position) {
                1 -> {
                    when (emotion.name) {
                        "happy" -> {
                            binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
                            binding.reactIv1.visibility = View.VISIBLE
                        }
                        "sad" -> {
                            binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                            binding.reactIv1.visibility = View.VISIBLE
                        }
                        "fear" -> {
                            binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                            binding.reactIv1.visibility = View.VISIBLE
                        }
                        "neutral" -> {
                            binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
                            binding.reactIv1.visibility = View.VISIBLE
                        }
                        "angry" -> {
                            binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
                            binding.reactIv1.visibility = View.VISIBLE
                        }
                        "surprise" -> {
                            binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                            binding.reactIv1.visibility = View.VISIBLE
                        }
                        "disgust" -> {
                            binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                            binding.reactIv1.visibility = View.VISIBLE
                        }
                    }
                }
                2 -> {
                    when (emotion.name) {
                        "happy" -> {
                            binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
                            binding.reactIv2.visibility = View.VISIBLE
                        }
                        "sad" -> {
                            binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                            binding.reactIv2.visibility = View.VISIBLE
                        }
                        "fear" -> {
                            binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                            binding.reactIv2.visibility = View.VISIBLE
                        }
                        "neutral" -> {
                            binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
                            binding.reactIv2.visibility = View.VISIBLE
                        }
                        "angry" -> {
                            binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
                            binding.reactIv2.visibility = View.VISIBLE
                        }
                        "surprise" -> {
                            binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                            binding.reactIv2.visibility = View.VISIBLE
                        }
                        "disgust" -> {
                            binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                            binding.reactIv2.visibility = View.VISIBLE
                        }
                    }
                }
                3 -> {
                    when (emotion.name) {
                        "happy" -> {
                            binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
                            binding.reactIv3.visibility = View.VISIBLE
                        }
                        "sad" -> {
                            binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                            binding.reactIv3.visibility = View.VISIBLE
                        }
                        "fear" -> {
                            binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                            binding.reactIv3.visibility = View.VISIBLE
                        }
                        "neutral" -> {
                            binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
                            binding.reactIv3.visibility = View.VISIBLE
                        }
                        "angry" -> {
                            binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
                            binding.reactIv3.visibility = View.VISIBLE
                        }
                        "surprise" -> {
                            binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                            binding.reactIv3.visibility = View.VISIBLE
                        }
                        "disgust" -> {
                            binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                            binding.reactIv3.visibility = View.VISIBLE
                        }
                    }
                }
                4 -> {
                    when (emotion.name) {
                        "happy" -> {
                            binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
                            binding.reactIv4.visibility = View.VISIBLE
                        }
                        "sad" -> {
                            binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                            binding.reactIv4.visibility = View.VISIBLE
                        }
                        "fear" -> {
                            binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                            binding.reactIv4.visibility = View.VISIBLE
                        }
                        "neutral" -> {
                            binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
                            binding.reactIv4.visibility = View.VISIBLE
                        }
                        "angry" -> {
                            binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
                            binding.reactIv4.visibility = View.VISIBLE
                        }
                        "surprise" -> {
                            binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                            binding.reactIv4.visibility = View.VISIBLE
                        }
                        "disgust" -> {
                            binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                            binding.reactIv4.visibility = View.VISIBLE
                        }
                    }
                }
                5 -> {
                    when (emotion.name) {
                        "happy" -> {
                            binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
                            binding.reactIv5.visibility = View.VISIBLE
                        }
                        "sad" -> {
                            binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                            binding.reactIv5.visibility = View.VISIBLE
                        }
                        "fear" -> {
                            binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                            binding.reactIv5.visibility = View.VISIBLE
                        }
                        "neutral" -> {
                            binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
                            binding.reactIv5.visibility = View.VISIBLE
                        }
                        "angry" -> {
                            binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
                            binding.reactIv5.visibility = View.VISIBLE
                        }
                        "surprise" -> {
                            binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                            binding.reactIv5.visibility = View.VISIBLE
                        }
                        "disgust" -> {
                            binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                            binding.reactIv5.visibility = View.VISIBLE
                        }
                    }
                }
                6 -> {
                    when (emotion.name) {
                        "happy" -> {
                            binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
                            binding.reactIv6.visibility = View.VISIBLE
                        }
                        "sad" -> {
                            binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                            binding.reactIv6.visibility = View.VISIBLE
                        }
                        "fear" -> {
                            binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                            binding.reactIv6.visibility = View.VISIBLE
                        }
                        "neutral" -> {
                            binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
                            binding.reactIv6.visibility = View.VISIBLE
                        }
                        "angry" -> {
                            binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
                            binding.reactIv6.visibility = View.VISIBLE
                        }
                        "surprise" -> {
                            binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                            binding.reactIv6.visibility = View.VISIBLE
                        }
                        "disgust" -> {
                            binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                            binding.reactIv6.visibility = View.VISIBLE
                        }
                    }
                }
                7 -> {
                    when (emotion.name) {
                        "happy" -> {
                            binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
                            binding.reactIv7.visibility = View.VISIBLE
                        }
                        "sad" -> {
                            binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                            binding.reactIv7.visibility = View.VISIBLE
                        }
                        "fear" -> {
                            binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                            binding.reactIv7.visibility = View.VISIBLE
                        }
                        "neutral" -> {
                            binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
                            binding.reactIv7.visibility = View.VISIBLE
                        }
                        "angry" -> {
                            binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
                            binding.reactIv7.visibility = View.VISIBLE
                        }
                        "surprise" -> {
                            binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                            binding.reactIv7.visibility = View.VISIBLE
                        }
                        "disgust" -> {
                            binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                            binding.reactIv7.visibility = View.VISIBLE
                        }
                    }
                }
            }
            position++
        }
    }

    private fun countReactions(reactionList: MutableList<Reactions>): List<Emoji> {
        for (emotion in reactionList) {

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

        return emojiCount.sortedByDescending { it.count }
    }

    override fun getItemCount(): Int {
        return postList.size
    }

}