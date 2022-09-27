package com.example.ulaugh.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.ulaugh.R
import com.example.ulaugh.databinding.AdapterHomeFlistBinding
import com.example.ulaugh.databinding.ItemGoogleAdBinding
import com.example.ulaugh.databinding.ItemMainBinding
import com.example.ulaugh.interfaces.OnClickListener
import com.example.ulaugh.interfaces.PostClickListener
import com.example.ulaugh.model.HomeRecyclerViewItem
import com.example.ulaugh.utils.Constants
import com.example.ulaugh.utils.Constants.TAG
import com.example.ulaugh.utils.Helper
import com.google.android.gms.ads.*
import jp.wasabeef.glide.transformations.BlurTransformation

class HomeAdapter(
    val context: Context,
    private val itemsList: ArrayList<HomeRecyclerViewItem>,
    private val onClickListener: OnClickListener,
    private val onPostClickListener: PostClickListener
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
        when (holder) {
            is HomeRecyclerViewHolder.AdsViewHolder -> holder.bind(
                context,
                itemsList[position] as HomeRecyclerViewItem.GoogleAds
            )
            is HomeRecyclerViewHolder.NewsViewHolder -> holder.bind(
                onPostClickListener,
                context,
                itemsList[position] as HomeRecyclerViewItem.SharePostData
            )
            is HomeRecyclerViewHolder.FriendsViewHolder -> holder.bind(
                context,
                itemsList[position] as HomeRecyclerViewItem.SuggestList,
                onClickListener
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

        class NewsViewHolder(private val binding: ItemMainBinding) :
            HomeRecyclerViewHolder(binding) {
            fun bind(
                onClickListener: PostClickListener,
                context: Context,
                post: HomeRecyclerViewItem.SharePostData
            ) {
                binding.nameTv.text = post.full_name
                binding.postDetail.text = post.description
                binding.tagsTv.text = post.tagsList

                val date = Helper.convertToLocal(post.date_time)
                binding.timeTv.text = Helper.covertTimeToText(date)
                binding.reactCount.text = Helper.prettyCount(post.reaction!!.size)
                if (post.reaction_type!!.isNotEmpty()) {
                    Glide.with(context)
                        .load(post.image_url)
                        .centerCrop()
                        .fitCenter()
                        .thumbnail()
                        .placeholder(R.drawable.seokangjoon)
                        .into(binding.coverPhoto)
                    binding.emojiTxt.visibility = View.GONE
                    binding.emoji.visibility = View.GONE
                    binding.reactedTxt.visibility = View.VISIBLE
                    binding.reactedEmoji.visibility = View.VISIBLE
//                    val str = "0x1F60A"
//                    val inter = java.lang.Long.parseLong("0x1F60A", 16)
//                    binding.reactedEmoji.text = String(Character.toChars(0x1F389))
                    binding.reactedEmoji.text = getEmojiByUnicode(post.reaction_type!!)
                }else{
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
                binding.coverPhoto.setOnClickListener {
                    onClickListener.onClick(post, Constants.POST)
                }
                binding.reactView.setOnClickListener {
                    onClickListener.onClick(post, Constants.REACTION)
                }
                binding.userPhoto.setOnClickListener {
                    onClickListener.onClick(post, Constants.PROFILE)
                }
//                binding.coverPhoto.setImageResource(R.drawable.seokangjoon)
//                binding.emoji.text = String(Character.toChars(0x1F60A))
            }
        }

        open fun getEmojiByUnicode(emoji: String): String? {
            var unicode = 0
            when(emoji){
                "angry"->{unicode = 0x1F621}
                "disgust"->{unicode = 0x1F621}
                "fear"->{unicode = 0x1F922}
                "happy"->{unicode = 0x1F602}
                "neutral"->{unicode = 0x1F611}
                "sad"->{unicode = 0x1F62A}
                "surprise"->{unicode = 0x1F631}
            }
            return String(Character.toChars(unicode))
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