package com.example.ulaugh.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ulaugh.R
import com.example.ulaugh.model.ChatModel
import com.example.ulaugh.utils.Helper

class MessageAdapter(
    var context: Context,
    private var chatModelList: List<ChatModel>,
    var senderFirebaseId: String,
) :
    RecyclerView.Adapter<MessageAdapter.MyViewHolder>() {
    //    @Inject
//    private lateinit var shareMemory:SharePref
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v: View

        return when (viewType) {
            SENT_MESSAGE -> {
                v = LayoutInflater.from(parent.context).inflate(
                    R.layout.item_container_sent_message,
                    parent,
                    false
                )
                SendMessage(v)
            }
            else -> {
                v = LayoutInflater.from(parent.context).inflate(
                    R.layout.item_container_receive_message,
                    parent,
                    false
                )
                ReceiveMessage(v)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val chat = chatModelList[position]

        when (holder.itemViewType) {
            SENT_MESSAGE -> {
                val vholder = holder as SendMessage
                vholder.messageText.text = chat.content_message
                vholder.timeText.text = Helper.convertToLocal(chat.date)
                if (chat.my_seen != null)
                    if (chat.my_seen)
                        vholder.isSeenText.visibility = View.VISIBLE
                    else
                        vholder.isSeenText.visibility = View.GONE
            }
            RECEIVE_MESSAGE -> {
                val vholder = holder as ReceiveMessage
                vholder.messageText.text = chat.content_message
                vholder.timeText.text = Helper.convertToLocal(chat.date)
//                Glide.with(context)
//                    .load(receiverPic)
//                    .centerCrop()
//                    .fitCenter()
//                    .thumbnail()
//                    .placeholder(R.drawable.user_logo)
//                    .into(vholder.receiverImage)
//                val filePath = "Photos/user_image$receiverFirebaseId"
//                val storageReference = FirebaseStorage.getInstance().getReference(filePath)
//                storageReference.downloadUrl.addOnSuccessListener { downloadUrl ->
//                    if (downloadUrl == null) {
//                        try {
////                            vholder.receiverImage.setImageResource(R.drawable.imageback_icon)
//                        } catch (e: NullPointerException) {
//
//                        }
//                    } else {
//                        try {
//                            Glide.with(context).load(downloadUrl).into(vholder.receiverImage)
//                        } catch (e: NullPointerException) {
//
//                        }
//                    }
//                }
            }
        }
    }

    override fun getItemCount(): Int {
        return chatModelList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatModelList[position].sender_firebase_id == senderFirebaseId) {
            SENT_MESSAGE
        } else {
            RECEIVE_MESSAGE
        }
    }

    open class MyViewHolder(v: View) : RecyclerView.ViewHolder(
        v
    )

    inner class SendMessage(view: View) : MyViewHolder(view) {
        var messageText: TextView = view.findViewById(R.id.message_text)
        var timeText: TextView = view.findViewById(R.id.time_text)
        var isSeenText: TextView = view.findViewById(R.id.seen_text)
//        var senderImage: CircularImageView = view.findViewById(R.id.send_chat_image)
    }

    inner class ReceiveMessage(view: View) : MyViewHolder(view) {
        var messageText: TextView = view.findViewById(R.id.message_text)
        var timeText: TextView = view.findViewById(R.id.time_text)
        var isSeenText: TextView = view.findViewById(R.id.seen_text)
//        var receiverImage: CircularImageView = view.findViewById(R.id.chat_receive_image)
    }

    companion object {
        const val SENT_MESSAGE = 0
        const val RECEIVE_MESSAGE = 1
    }
}