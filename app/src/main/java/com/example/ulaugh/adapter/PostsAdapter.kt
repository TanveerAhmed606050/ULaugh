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
import com.example.ulaugh.fragment.ProfileFragment
import com.example.ulaugh.interfaces.PostClickListener
import com.example.ulaugh.model.PostItem
import com.example.ulaugh.utils.Constants
import com.google.gson.Gson
import com.makeramen.roundedimageview.RoundedImageView

class PostsAdapter internal constructor(
    private val mContext: Context,
    private val postItems: MutableList<PostItem>,
    private val mClickListener: PostClickListener
) :
    RecyclerView.Adapter<PostsAdapter.ListViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(mContext)

    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val postImageView: RoundedImageView = itemView.findViewById(R.id.imagePost)

        fun setPostImage(postItem: PostItem) {
//            Glide
//                .with(context)
//                .load("http://health.mlbranch.com/assets/images/lIJxNw16D3BL.jpg")
//                .into(postImageView)

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
//                intent.putExtra(Constants.POST, Gson().toJson(postItem))
                mContext.startActivity(intent)
            }
        }
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