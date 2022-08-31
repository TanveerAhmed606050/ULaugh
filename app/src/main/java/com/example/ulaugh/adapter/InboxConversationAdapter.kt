package com.example.ulaugh.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ulaugh.R
import com.example.ulaugh.databinding.ItemContainerRecentConversionBinding
import com.example.ulaugh.model.InboxListModel
import com.google.firebase.storage.FirebaseStorage

class InboxConversationAdapter(var context: Context, var usersList: List<InboxListModel>) :
    RecyclerView.Adapter<InboxConversationAdapter.ViewHolder>() {
    private var _binding: ItemContainerRecentConversionBinding? = null
    private val binding get() = _binding!!
    var friendid: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        _binding = ItemContainerRecentConversionBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    inner class ViewHolder(binding: ItemContainerRecentConversionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val inbox = usersList[position]
        binding.muftiNameText.text = inbox.other_user_firebase_name
        binding.chatTimeText.text = inbox.latest_message.date
        friendid = inbox.other_user_firebase_id
        binding.chatMessageText.text = inbox.latest_message.message

        val filePath="Photos/"+"user_image"+inbox.other_user_firebase_id
        val storageReference = FirebaseStorage.getInstance().getReference(filePath)
        storageReference.downloadUrl.addOnSuccessListener { downloadUrl ->
            if (downloadUrl == null) {
                try {
                    binding.muftiProfileImage.setImageResource(R.drawable.back_btn)
                } catch (e: NullPointerException) {

                }
            } else {
                try {
                    Glide.with(context).load(downloadUrl).into(binding.muftiProfileImage)
                } catch (e: NullPointerException) {

                }
            }
        }


    }

    override fun getItemCount(): Int {
        return usersList.size
    }

//    inner class MyViewHolder(itemView: View) :
//        RecyclerView.ViewHolder(itemView), View.OnClickListener {
//        var userName: TextView = itemView.findViewById(R.id.mufti_name_text)
//        var timeText: TextView = itemView.findViewById(R.id.chat_time_text)
//        var lastMessage: TextView = itemView.findViewById(R.id.chat_message_text)
//        var imageView: CircularImageView = itemView.findViewById(R.id.mufti_profile_image)
////        var image_off: CircularImageView = itemView.findViewById(R.id.chat_offline_icon)
////        var image_on: CircularImageView = itemView.findViewById(R.id.chat_online_icon)
//        override fun onClick(view: View) {
//            val users = usersList[adapterPosition]
//            friendid = users.other_user_firebase_id
//            val conversationId = users.conversation_id
////            val intent = Intent(context, ChatActivity::class.java)
////            intent.putExtra("firebaseId", friendid)
////            intent.putExtra("conversationId", conversationId)
////            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
////            context.startActivity(intent)
//        }
//
//        init {
//            itemView.setOnClickListener(this)
//        }
//    }
}
