package com.example.ulaugh.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ulaugh.R
import com.example.ulaugh.controller.ChatActivity
import com.example.ulaugh.model.InboxListModel
import com.example.ulaugh.utils.Constants
import com.example.ulaugh.utils.Helper
import com.google.firebase.storage.FirebaseStorage
import com.mikhaellopez.circularimageview.CircularImageView

class InboxConversationAdapter(var context: Context, var usersList: List<InboxListModel>) :
    RecyclerView.Adapter<InboxConversationAdapter.MyViewHolder>() {
    var friendid: String? = null
    fun filterList(filterList: ArrayList<InboxListModel>) {
        // below line is to add our filtered
        // list in our course array list.
        usersList = filterList
        // below line is to notify our adapter
        // as change in recycler view data.
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.item_container_recent_conversion, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val inbox = usersList[position]
        holder.userName.text = inbox.other_user_firebase_name
        if (inbox.unReadMsg != null)
            if (inbox.unReadMsg)
                holder.unReadMsgTv.visibility = View.VISIBLE
//        Log.d("lskjdgla", "inbox: ${inbox.latest_message.message}")
        holder.timeText.text = Helper.convertToLocal(inbox.latest_message!!.date)
        friendid = inbox.other_user_firebase_id
        holder.lastMessage.text = inbox.latest_message!!.message

        Glide.with(context)
            .load(inbox.profile_pic)
            .centerCrop()
            .fitCenter()
            .thumbnail()
            .placeholder(R.drawable.user_logo)
            .into(holder.imageView)

        holder.itemView.setOnClickListener {
            val receiverFirebaseId = inbox.other_user_firebase_id
            val conversationId = inbox.conversation_id
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(Constants.FIREBASE_ID, receiverFirebaseId)
            intent.putExtra("conversationId", conversationId)
            intent.putExtra(Constants.MESSAGE_TOKEN, inbox.message_token)
            intent.putExtra("IsChecked", false)
            intent.putExtra("receiverName", inbox.other_user_firebase_name)
            intent.putExtra(Constants.PROFILE_PIC, inbox.profile_pic)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return usersList.size
    }

    inner class MyViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var userName: TextView = itemView.findViewById(R.id.mufti_name_text)
        var timeText: TextView = itemView.findViewById(R.id.chat_time_text)
        var unReadMsgTv: ImageView = itemView.findViewById(R.id.un_read_msg)
        var lastMessage: TextView = itemView.findViewById(R.id.chat_message_text)
        var imageView: CircularImageView = itemView.findViewById(R.id.mufti_profile_image)
//        var view: ConstraintLayout = itemView.findViewById(R.id.container)
    }
}
