package com.example.ulaugh.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.ulaugh.R
import com.example.ulaugh.controller.CameraActivity
import com.example.ulaugh.controller.ReactDetailActivity
import com.example.ulaugh.databinding.AdapterHomeFlistBinding
import com.example.ulaugh.databinding.ItemGoogleAdBinding
import com.example.ulaugh.databinding.ItemMainBinding
import com.example.ulaugh.interfaces.OnClickListener
import com.example.ulaugh.model.HomeRecyclerViewItem
import com.example.ulaugh.utils.Constants.TAG
import com.google.android.gms.ads.*

class HomeAdapter(
    val context: Context,
    private val itemsList: ArrayList<HomeRecyclerViewItem>,
    val onClickListener: OnClickListener
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
                context,
                itemsList[position] as HomeRecyclerViewItem.NewsFeed
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
            is HomeRecyclerViewItem.NewsFeed -> R.layout.item_main
            is HomeRecyclerViewItem.SuggestList -> R.layout.adapter_home_flist
            else -> {Log.d("", "error")}
        }
    }

    sealed class HomeRecyclerViewHolder(binding: ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        class NewsViewHolder(private val binding: ItemMainBinding) :
            HomeRecyclerViewHolder(binding) {
            fun bind(context: Context, newsFeed: HomeRecyclerViewItem.NewsFeed) {
                binding.coverPhoto.setOnClickListener {
                    context.startActivity(Intent(context, ReactDetailActivity::class.java))
                }
                binding.reactView.setOnClickListener {
                    context.startActivity(Intent(context, CameraActivity::class.java))
                }
//                binding.coverPhoto.setImageResource(R.drawable.seokangjoon)
//                binding.emoji.text = String(Character.toChars(0x1F60A))
            }
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
            fun bind(mContext: Context, friends: HomeRecyclerViewItem.SuggestList, onClickListener: OnClickListener) {
                binding.rv.apply {
                    setHasFixedSize(true)
                    adapter = HomeFriendsAdapter(mContext, friends, onClickListener)
                }
            }
        }
    }

}