package com.example.ulaugh.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ulaugh.R
import com.example.ulaugh.databinding.AdapterRewardBinding
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
        if (emotionsList.isNotEmpty())
            setEmotions(emotionsList, context)
        if (userPost.reaction!!.size < 1000) {
            binding.claimBtn.background = context.getDrawable(R.drawable.grey_rc)
            binding.claimBtn.isEnabled = false
        }
    }

    private fun setEmotions(emotionsList: List<Pair<String?, Int>>, context: Context) {
        var position = 1 //set half emotions
        for (emotion in emotionsList) {
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
                        "surprise" -> binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
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
                        "surprise" -> binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
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
                        "surprise" -> binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
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
                        "surprise" -> binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                        "disgust" -> binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                    }
                    binding.reactIv7.visibility = View.VISIBLE
                }
            }
            position++
        }
    }

    private fun countReactions(reactionList: MutableList<Reactions>): List<Pair<String?, Int>> {
        val frequencies = reactionList.groupingBy { it.reaction_type }.eachCount()
        return frequencies.toList().sortedByDescending { (key, value) -> value }
    }

    override fun getItemCount(): Int {
        return postList.size
    }

}