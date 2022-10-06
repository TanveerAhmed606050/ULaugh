package com.example.ulaugh.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ulaugh.R
import com.example.ulaugh.model.ChatModel
import com.example.ulaugh.utils.Constants
import com.example.ulaugh.utils.SharePref
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import com.mikhaellopez.circularimageview.CircularImageView
import javax.inject.Inject

class MessageAdapter(
    var context: Context,
    var chatModelList: List<ChatModel>,
    var receiverFirebaseId: String,
    var senderImageUrl: String
) :
    RecyclerView.Adapter<MessageAdapter.MyViewHolder>() {
//    @Inject
//    private lateinit var shareMemory:SharePref
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v: View

        return when (viewType) {
            SENT_MESSAGE -> {
                v = LayoutInflater.from(parent.context).inflate(R.layout.item_container_sent_message, parent, false)
                SendMessage(v)
            }
            else -> {
                v = LayoutInflater.from(parent.context).inflate(R.layout.item_container_receive_message, parent, false)
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
                vholder.timeText.text = chat.date

//                if (shareMemory.readString(Constants.PROFILE, "")!!.isNotEmpty()) {
//                    try {
//                        Glide.with(context)
//                            .load(shareMemory.readString(Constants.PROFILE, ""))
//                            .placeholder(R.drawable.imageback_icon)
//                            .into(vholder.senderImage)
//                    }catch (e:NullPointerException){
//
//                    }
//
//                }


            }
            RECEIVE_MESSAGE -> {
                val vholder = holder as ReceiveMessage
                vholder.messageText.text = chat.content_message
                vholder.timeText.text = chat.date
                val filePath="Photos/"+"user_image"+receiverFirebaseId
                val storageReference = FirebaseStorage.getInstance().getReference(filePath)
                storageReference.downloadUrl.addOnSuccessListener(object : OnSuccessListener<Uri?> {
                    override fun onSuccess(downloadUrl: Uri?) {
                        if (downloadUrl == null) {
                            try {
                                vholder.receiverImage.setImageResource(R.drawable.imageback_icon)
                            } catch (e: NullPointerException) {

                            }
                        } else {
                            try {
                                Glide.with(context).load(downloadUrl).into(vholder.receiverImage)
                            } catch (e: NullPointerException) {

                            }
                        }
                    }
                })




            }
        }

    }

    override fun getItemCount(): Int {
        return chatModelList.size
    }

    override fun getItemViewType(position: Int): Int {
//        return if (chatModelList[position].sender_firebase_id == shareMemory.readString(Constants.FIREBASE_ID, "")) {
//            SENT_MESSAGE
//        } else {
//            RECEIVE_MESSAGE
//        }
        return RECEIVE_MESSAGE
    }


    open class MyViewHolder(v: View) : RecyclerView.ViewHolder(
        v


    )

    inner class SendMessage(view: View) : MyViewHolder(view) {
        var messageText: TextView = view.findViewById(R.id.message_text)
        var timeText: TextView = view.findViewById(R.id.time_text)
        var isSeenText: TextView = view.findViewById(R.id.seen_text)
        var senderImage: CircularImageView = view.findViewById(R.id.send_chat_image)

    }

    inner class ReceiveMessage(view: View) : MyViewHolder(view) {
        var messageText: TextView = view.findViewById(R.id.message_text)
        var timeText: TextView = view.findViewById(R.id.time_text)
        var isSeenText: TextView = view.findViewById(R.id.seen_text)
        var receiverImage: CircularImageView = view.findViewById(R.id.chat_receive_image)


    }

    companion object {
        const val SENT_MESSAGE = 0
        const val RECEIVE_MESSAGE = 1
    }
}