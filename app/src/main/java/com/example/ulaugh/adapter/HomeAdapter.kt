package com.example.ulaugh.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.ulaugh.R
import com.example.ulaugh.databinding.AdapterHomeFlistBinding
import com.example.ulaugh.databinding.ItemGoogleAdBinding
import com.example.ulaugh.databinding.ItemMainBinding
import com.example.ulaugh.interfaces.FollowFriendListener
import com.example.ulaugh.interfaces.OnClickListener
import com.example.ulaugh.interfaces.AddFriendListener
import com.example.ulaugh.model.HomeRecyclerViewItem
import com.example.ulaugh.model.Reactions
import com.example.ulaugh.utils.Constants
import com.example.ulaugh.utils.Constants.TAG
import com.example.ulaugh.utils.Helper
import com.google.android.gms.ads.*
import com.google.firebase.auth.FirebaseAuth
import jp.wasabeef.glide.transformations.BlurTransformation

class HomeAdapter(
    val context: Context,
    private val itemsList: ArrayList<HomeRecyclerViewItem>,
    private val onClickListener: OnClickListener,
    private val onPostClickListener: AddFriendListener,
    private val followFriendListener: FollowFriendListener
) :
    RecyclerView.Adapter<HomeAdapter.HomeRecyclerViewHolder>() {
//    var items = listOf<HomeRecyclerViewItem>()
//        set(value) {
//            field = value
//            notifyDataSetChanged()
//        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeRecyclerViewHolder {
        return when (viewType) {
            R.layout.item_main -> HomeRecyclerViewHolder.NewsViewHolder(
                ItemMainBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

//            return ViewHolder(LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.item_main, parent, false), viewType)
//                )
            R.layout.item_google_ad -> HomeRecyclerViewHolder.AdsViewHolder(
                ItemGoogleAdBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            R.layout.adapter_home_flist -> HomeRecyclerViewHolder.FriendsViewHolder(
                AdapterHomeFlistBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> throw IllegalArgumentException("Invalid ViewType Provided")
        }
    }

    override fun onBindViewHolder(holder: HomeRecyclerViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        when (holder) {
            is HomeRecyclerViewHolder.AdsViewHolder -> holder.bind(
                context,
                itemsList[position] as HomeRecyclerViewItem.GoogleAds
            )
            is HomeRecyclerViewHolder.NewsViewHolder -> holder.bind(
                onPostClickListener,
                context,
                itemsList[position] as HomeRecyclerViewItem.SharePostData,
                followFriendListener
            )
            is HomeRecyclerViewHolder.FriendsViewHolder -> holder.bind(
                context,
                itemsList[position] as HomeRecyclerViewItem.SuggestList,
                onClickListener,
            )
        }
    }

    override fun getItemCount() = itemsList.size

    override fun getItemViewType(position: Int): Int {
        return when (itemsList[position]) {
            is HomeRecyclerViewItem.GoogleAds -> R.layout.item_google_ad
            is HomeRecyclerViewItem.SharePostData -> R.layout.item_main
            is HomeRecyclerViewItem.SuggestList -> R.layout.adapter_home_flist
            else -> {
                Log.d("", "error")
            }
        }
    }

    sealed class HomeRecyclerViewHolder(binding: ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
//        private val emoji = Emoji("", 0)
//        private val emojiCount = ArrayList<Emoji>()

        class NewsViewHolder(private val binding: ItemMainBinding) :
            HomeRecyclerViewHolder(binding) {
            fun bind(onClickListener: AddFriendListener,
                     context: Context,
                     post: HomeRecyclerViewItem.SharePostData,
                     followFriendListener: FollowFriendListener) {
                binding.nameTv.text = post.full_name
                binding.postDetail.text = post.description
                binding.tagsTv.text = post.tagsList

                if (post.is_follow!! || post.firebase_id != FirebaseAuth.getInstance().currentUser!!.uid) {
                    binding.plusIv.visibility = View.GONE
                    binding.followTv.visibility = View.GONE
                }
                val date = Helper.convertToLocal(post.date_time)
                binding.timeTv.text = Helper.covertTimeToText(date)
                binding.reactCount.text = Helper.prettyCount(post.reaction!!.size)
                val emotionsList = countReactions(post.reaction!!)
                if (emotionsList.isNotEmpty())
                    setEmotions(emotionsList, context, binding)
                if (post.reaction_type!!.isNotEmpty()) {
                    Glide.with(context)
                        .load(post.image_url)
                        .centerCrop()
                        .fitCenter()
                        .thumbnail()
                        .placeholder(R.drawable.user_logo)
                        .into(binding.coverPhoto)
                    binding.emojiTxt.visibility = View.GONE
                    binding.emoji.visibility = View.GONE
                    binding.reactedTxt.visibility = View.VISIBLE
                    binding.reactedEmoji.visibility = View.VISIBLE
//                    val str = "0x1F60A"
//                    val inter = java.lang.Long.parseLong("0x1F60A", 16)
//                    binding.reactedEmoji.text = String(Character.toChars(0x1F389))
                    setSelfReactEmoji(binding.reactedEmoji, post.reaction_type!!, context)
                } else {
                    Glide.with(context)
                        .load(post.image_url)
                        .centerCrop()
                        .fitCenter()
                        .thumbnail()
                        .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 4)))
                        .placeholder(R.drawable.seokangjoon)
                        .into(binding.coverPhoto)
                    binding.emojiTxt.visibility = View.VISIBLE
                    binding.emoji.visibility = View.VISIBLE
                    binding.reactedTxt.visibility = View.GONE
                    binding.reactedEmoji.visibility = View.GONE
                }
                Glide.with(context)
                    .load(post.profile_image)
                    .centerCrop()
                    .fitCenter()
                    .thumbnail()
                    .placeholder(R.drawable.seokangjoon)
                    .into(binding.userPhoto)
                binding.coverPhoto.setOnClickListener {
                    onClickListener.onClick(post, Constants.POST, emotionsList)
                }
                binding.reactView.setOnClickListener {
                    onClickListener.onClick(post, Constants.REACTION)
                }
                binding.userPhoto.setOnClickListener {
                    onClickListener.onClick(post, Constants.PROFILE)
                }
                binding.followView.setOnClickListener {
                    if (post.firebase_id != FirebaseAuth.getInstance().currentUser!!.uid)
                        followFriendListener.onFollow(post.firebase_id, Constants.REJECTED)
//                    onClickListener.onClick(post, Constants.FOLLOW)
                }
            }

            private fun setEmotions(
                emotionsList: List<Pair<String?, Int>>,
                context: Context,
                binding: ItemMainBinding
            ) {
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
                                "surprise" -> binding.reactIv1.setImageDrawable(
                                    context.getDrawable(
                                        R.drawable.fear_ic
                                    )
                                )
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
                                "surprise" -> binding.reactIv2.setImageDrawable(
                                    context.getDrawable(
                                        R.drawable.fear_ic
                                    )
                                )
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
                                "surprise" -> binding.reactIv3.setImageDrawable(
                                    context.getDrawable(
                                        R.drawable.fear_ic
                                    )
                                )
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
//                    when (position) {
//                        1 -> {
//                            when (emotion.first) {
//                                "happy" -> {
//                                    binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
//                                    binding.reactIv1.visibility = View.VISIBLE
//                                }
//                                "sad" -> {
//                                    binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
//                                    binding.reactIv1.visibility = View.VISIBLE
//                                }
//                                "fear" -> {
//                                    binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
//                                    binding.reactIv1.visibility = View.VISIBLE
//                                }
//                                "neutral" -> {
//                                    binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
//                                    binding.reactIv1.visibility = View.VISIBLE
//                                }
//                                "angry" -> {
//                                    binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
//                                    binding.reactIv1.visibility = View.VISIBLE
//                                }
//                                "surprise" -> {
//                                    binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
//                                    binding.reactIv1.visibility = View.VISIBLE
//                                }
//                                "disgust" -> {
//                                    binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
//                                    binding.reactIv1.visibility = View.VISIBLE
//                                }
//                            }
//                        }
//                        2 -> {
//                            when (emotion.name) {
//                                "happy" -> {
//                                    binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
//                                    binding.reactIv2.visibility = View.VISIBLE
//                                }
//                                "sad" -> {
//                                    binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
//                                    binding.reactIv2.visibility = View.VISIBLE
//                                }
//                                "fear" -> {
//                                    binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
//                                    binding.reactIv2.visibility = View.VISIBLE
//                                }
//                                "neutral" -> {
//                                    binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
//                                    binding.reactIv2.visibility = View.VISIBLE
//                                }
//                                "angry" -> {
//                                    binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
//                                    binding.reactIv2.visibility = View.VISIBLE
//                                }
//                                "surprise" -> {
//                                    binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
//                                    binding.reactIv2.visibility = View.VISIBLE
//                                }
//                                "disgust" -> {
//                                    binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
//                                    binding.reactIv2.visibility = View.VISIBLE
//                                }
//                            }
//                        }
//                        3 -> {
//                            when (emotion.name) {
//                                "happy" -> {
//                                    binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
//                                    binding.reactIv3.visibility = View.VISIBLE
//                                }
//                                "sad" -> {
//                                    binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
//                                    binding.reactIv3.visibility = View.VISIBLE
//                                }
//                                "fear" -> {
//                                    binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
//                                    binding.reactIv3.visibility = View.VISIBLE
//                                }
//                                "neutral" -> {
//                                    binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
//                                    binding.reactIv3.visibility = View.VISIBLE
//                                }
//                                "angry" -> {
//                                    binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
//                                    binding.reactIv3.visibility = View.VISIBLE
//                                }
//                                "surprise" -> {
//                                    binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
//                                    binding.reactIv3.visibility = View.VISIBLE
//                                }
//                                "disgust" -> {
//                                    binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
//                                    binding.reactIv3.visibility = View.VISIBLE
//                                }
//                            }
//                        }
//                        4 -> {
//                            when (emotion.name) {
//                                "happy" -> {
//                                    binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
//                                    binding.reactIv4.visibility = View.VISIBLE
//                                }
//                                "sad" -> {
//                                    binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
//                                    binding.reactIv4.visibility = View.VISIBLE
//                                }
//                                "fear" -> {
//                                    binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
//                                    binding.reactIv4.visibility = View.VISIBLE
//                                }
//                                "neutral" -> {
//                                    binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
//                                    binding.reactIv4.visibility = View.VISIBLE
//                                }
//                                "angry" -> {
//                                    binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
//                                    binding.reactIv4.visibility = View.VISIBLE
//                                }
//                                "surprise" -> {
//                                    binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
//                                    binding.reactIv4.visibility = View.VISIBLE
//                                }
//                                "disgust" -> {
//                                    binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
//                                    binding.reactIv4.visibility = View.VISIBLE
//                                }
//                            }
//                        }
//                        5 -> {
//                            when (emotion.name) {
//                                "happy" -> {
//                                    binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
//                                    binding.reactIv5.visibility = View.VISIBLE
//                                }
//                                "sad" -> {
//                                    binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
//                                    binding.reactIv5.visibility = View.VISIBLE
//                                }
//                                "fear" -> {
//                                    binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
//                                    binding.reactIv5.visibility = View.VISIBLE
//                                }
//                                "neutral" -> {
//                                    binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
//                                    binding.reactIv5.visibility = View.VISIBLE
//                                }
//                                "angry" -> {
//                                    binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
//                                    binding.reactIv5.visibility = View.VISIBLE
//                                }
//                                "surprise" -> {
//                                    binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
//                                    binding.reactIv5.visibility = View.VISIBLE
//                                }
//                                "disgust" -> {
//                                    binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
//                                    binding.reactIv5.visibility = View.VISIBLE
//                                }
//                            }
//                        }
//                        6 -> {
//                            when (emotion.name) {
//                                "happy" -> {
//                                    binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
//                                    binding.reactIv6.visibility = View.VISIBLE
//                                }
//                                "sad" -> {
//                                    binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
//                                    binding.reactIv6.visibility = View.VISIBLE
//                                }
//                                "fear" -> {
//                                    binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
//                                    binding.reactIv6.visibility = View.VISIBLE
//                                }
//                                "neutral" -> {
//                                    binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
//                                    binding.reactIv6.visibility = View.VISIBLE
//                                }
//                                "angry" -> {
//                                    binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
//                                    binding.reactIv6.visibility = View.VISIBLE
//                                }
//                                "surprise" -> {
//                                    binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
//                                    binding.reactIv6.visibility = View.VISIBLE
//                                }
//                                "disgust" -> {
//                                    binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
//                                    binding.reactIv6.visibility = View.VISIBLE
//                                }
//                            }
//                        }
//                        7 -> {
//                            when (emotion.name) {
//                                "happy" -> {
//                                    binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
//                                    binding.reactIv7.visibility = View.VISIBLE
//                                }
//                                "sad" -> {
//                                    binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
//                                    binding.reactIv7.visibility = View.VISIBLE
//                                }
//                                "fear" -> {
//                                    binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
//                                    binding.reactIv7.visibility = View.VISIBLE
//                                }
//                                "neutral" -> {
//                                    binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
//                                    binding.reactIv7.visibility = View.VISIBLE
//                                }
//                                "angry" -> {
//                                    binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
//                                    binding.reactIv7.visibility = View.VISIBLE
//                                }
//                                "surprise" -> {
//                                    binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
//                                    binding.reactIv7.visibility = View.VISIBLE
//                                }
//                                "disgust" -> {
//                                    binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
//                                    binding.reactIv7.visibility = View.VISIBLE
//                                }
//                            }
//                        }
//                    }
//                    position++

            }
        }

        fun countReactions(reactionList: MutableList<Reactions>): List<Pair<String?, Int>> {
//            for (emotion in reactionList) {
//                run {
//                    Log.d(TAG, "Neutral: ${emojiCount.size}")
//                    if (emojiCount.any { it.name == "neutral" } && emotion.reaction_type == "neutral") {
//                        emojiCount.find { it.name == "neutral" }!!.count++
////                        Log.d(TAG, "Neutral: ${emojiCount.size}")
//                    } else if (emotion.reaction_type == "neutral") {
//                        val emoji = Emoji("neutral", 1)
//                        emojiCount.add(emoji)
//                    } else if (emojiCount.any { it.name == "happy" } && emotion.reaction_type == "happy") {
//                        emojiCount.find { it.name == "happy" }!!.count++
//                    } else if (emotion.reaction_type == "happy") {
//                        val emoji = Emoji("happy", 1)
//                        emojiCount.add(emoji)
//                    } else if (emojiCount.any { it.name == "sad" } && emotion.reaction_type == "sad") {
//                        emojiCount.find { it.name == "sad" }!!.count++
//                    } else if (emotion.reaction_type == "sad") {
//                        val emoji = Emoji("sad", 1)
//                        emojiCount.add(emoji)
//                    } else if (emojiCount.any { it.name == "surprise" } && emotion.reaction_type == "surprise") {
//                        emojiCount.find { it.name == "surprise" }!!.count++
//                    } else if (emotion.reaction_type == "surprise") {
//                        val emoji = Emoji("surprise", 1)
//                        emojiCount.add(emoji)
//                    } else if (emojiCount.any { it.name == "angry" } && emotion.reaction_type == "angry") {
//                        emojiCount.find { it.name == "angry" }!!.count++
//                    } else if (emotion.reaction_type == "angry") {
//                        val emoji = Emoji("angry", 1)
//                        emojiCount.add(emoji)
//                    } else if (emojiCount.any { it.name == "fear" } && emotion.reaction_type == "fear") {
//                        emojiCount.find { it.name == "fear" }!!.count++
//                    } else if (emotion.reaction_type == "fear") {
//                        val emoji = Emoji("fear", 1)
//                        emojiCount.add(emoji)
//                    } else if (emojiCount.any { it.name == "disgust" } && emotion.reaction_type == "disgust") {
//                        emojiCount.find { it.name == "disgust" }!!.count++
//                    } else {
//                        val emoji = Emoji("disgust", 1)
//                        emojiCount.add(emoji)
//                    }
//                }
//            }

            val frequencies = reactionList.groupingBy { it.reaction_type }.eachCount()
            //            val output = reactionList.groupBy { it }
//                .map { "${it.key} : ${it.value.size}" }
//                .toString()
//            Log.d(TAG, "sorted: $sorted")

            return frequencies.toList().sortedByDescending { (key, value) -> value }
        }

        open fun setSelfReactEmoji(reactView: ImageView, emoji: String, context: Context) {
//            var unicode = 0
            when (emoji) {
                "angry" -> {
                    reactView.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
//                    unicode = 0x1F621
                }
                "disgust" -> {
//                    unicode = 0x1F621
                    reactView.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                }
                "fear" -> {
//                    unicode = 0x1F922
                    reactView.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                }
                "happy" -> {
//                    unicode = 0x1F602
                    reactView.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
                }
                "neutral" -> {
//                    unicode = 0x1F611
                    reactView.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
                }
                "sad" -> {
//                    unicode = 0x1F62A
                    reactView.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                }
                "surprise" -> {
//                    unicode = 0x1F631
                    reactView.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                }
            }
//            return String(Character.toChars(unicode))
        }

        class AdsViewHolder(private val binding: ItemGoogleAdBinding) :
            HomeRecyclerViewHolder(binding) {
            fun bind(context: Context, ads: HomeRecyclerViewItem.GoogleAds) {
                initAd(context)
//                binding.imageViewMovie.loadImage(movie.thumbnail)
            }

            private fun initAd(context: Context) {
                MobileAds.initialize(context) {}

                val adRequest = AdRequest.Builder().build()
                binding.adView.loadAd(adRequest)
                val adView = AdView(context)

                adView.setAdSize(AdSize.BANNER)

                adView.adUnitId = "${R.string.banner_ad_unit_id}"

                binding.adView.adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        // Code to be executed when an ad finishes loading.
                        binding.adView.visibility = View.VISIBLE
                        Log.d(TAG, "AdLoaded")
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        // Code to be executed when an ad request fails.
                        Log.d(TAG, "AdFailedToLoad $adError")
                    }

                    override fun onAdOpened() {
                        // Code to be executed when an ad opens an overlay that
                        // covers the screen.
                        Log.d(TAG, "AdOpened")
                    }

                    override fun onAdClicked() {
                        // Code to be executed when the user clicks on an ad.
                        Log.d(TAG, "AdClicked")
                    }

                    override fun onAdClosed() {
                        // Code to be executed when the user is about to return
                        // to the app after tapping on an ad.
                        Log.d(TAG, "AdClosed")
                    }
                }

            }
        }

        class FriendsViewHolder(private val binding: AdapterHomeFlistBinding) :
            HomeRecyclerViewHolder(binding) {
            fun bind(
                mContext: Context,
                friends: HomeRecyclerViewItem.SuggestList,
                onClickListener: OnClickListener
            ) {
                binding.rv.apply {
                    setHasFixedSize(true)
                    adapter = HomeFriendsAdapter(mContext, friends, onClickListener)
                }
            }
        }
    }

}