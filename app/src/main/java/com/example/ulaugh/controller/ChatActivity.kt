package com.example.ulaugh.controller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.ulaugh.R
import com.example.ulaugh.adapter.MessageAdapter
import com.example.ulaugh.databinding.ActivityChatBinding
import com.example.ulaugh.model.ChatModel
import com.example.ulaugh.model.UserRequest
import com.example.ulaugh.utils.Constants
import com.example.ulaugh.utils.FirebaseAuth
import com.example.ulaugh.utils.Helper
import com.example.ulaugh.utils.SharePref
import com.google.firebase.database.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {

    @Inject
    lateinit var sharePref: SharePref
    private var _binding: ActivityChatBinding? = null
    private val binding get() = _binding!!
    private var profileDetail: UserRequest? = null
    private lateinit var userRefForSeen: DatabaseReference
    private lateinit var receiverReference: DatabaseReference
    private lateinit var senderReference: DatabaseReference
    var chatModelList: MutableList<ChatModel> = ArrayList()
    lateinit var messageAdapter: MessageAdapter
    var senderFirebaseId = ""
    var receiverFirebaseId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        clickListener()
        initView()
    }

    private fun initView() {
        if (intent != null)
            profileDetail =
                Gson().fromJson(
                    intent.getStringExtra(Constants.PROFILE),
                    object : TypeToken<UserRequest>() {}.type
                )
        if (profileDetail != null) {
            binding.nameTv.text = profileDetail!!.full_name
            Glide.with(this)
                .load(profileDetail!!.profile_pic)
                .centerCrop()
                .fitCenter()
                .thumbnail()
                .placeholder(R.drawable.seokangjoon)
                .into(binding.profileIv)
            senderFirebaseId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser!!.uid
            receiverFirebaseId = profileDetail!!.firebase_id!!
        }
    }

    private fun clickListener() {
        binding.backBtn.setOnClickListener {
            finish()
        }
        binding.menuIv.setOnClickListener { }
        binding.sendIv.setOnClickListener {
            val message = binding.chatEt.text.toString()
            val time = Helper().localToGMT()

            binding.chatEt.text = null
            val messageType = "text"
//            sendMessageFun(
//                senderFirebaseId,
//                receiverFirebaseId,
//                message,
//                time,
//                messageType
//            )
        }
    }

//    private fun sendMessageFun(
//        senderFirebaseId: String,
//        receiverFirebaseId: String,
//        message: String,
//        dateAndTime: String,
//        messageType: String
//    ) {
//
//        val senderConversionHashmap =
//            firebaseDataBaseHashmapFunctionClass.inboxConversationHashmapFun(
//                messageId,
//                receiverFirebaseId,
//                receiverName,
//                receiverPersonalId,
//                "false",
//                dateAndTime,
//                message,
//                messageType
//            )
//        val receiverConversionHashmap =
//            firebaseDataBaseHashmapFunctionClass.inboxConversationHashmapFun(
//                messageId,
//                senderFirebaseId,
//                senderName,
//                senderPersonalId,
//                "false",
//                dateAndTime,
//                message,
//                messageType
//            )
//
//        val senderDatabaseReference =
//            FirebaseDatabase.getInstance().getReference("Inbox").child(senderFirebaseId)
//                .child("conversations").child(messageId)
//        senderDatabaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                senderDatabaseReference.setValue(senderConversionHashmap)
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//
//            }
//
//        })
//
//
//        val receiverDatabaseReference =
//            FirebaseDatabase.getInstance().getReference("Inbox").child(receiverFirebaseId)
//                .child("conversations").child(messageId)
//
//        receiverDatabaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                receiverDatabaseReference.setValue(receiverConversionHashmap)
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//
//            }
//
//        })
//
//        val messageHashmap = firebaseDataBaseHashmapFunctionClass.messageHashmapFun(
//            messageId,
//            messageType,
//            message,
//            dateAndTime,
//            senderFirebaseId,
//            senderName,
//            senderPersonalId,
//            receiverFirebaseId,
//            receiverPersonalId,
//            receiverName,
//            "false"
//        )
//        val messageReference =
//            FirebaseDatabase.getInstance()
//                .getReference("All_Conversations")
//                .child(messageId)
//                .child("messages")
//
//
//
//        messageReference.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                messageReference.push().setValue(messageHashmap)
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//
//            }
//
//        })
//        readMessagesFromAllConversation(messageId)
//
//
//    }
//
//    private fun readMessagesFromAllConversation(messageId: String) {
//        val messageReference = FirebaseDatabase.getInstance().getReference("All_Conversations")
//            .child(messageId)
//            .child("messages")
//        messageReference.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                chatModelList.clear()
//                for (ds in snapshot.children) {
//                    val chatModel: ChatModel? = ds.getValue(ChatModel::class.java)
//
//                    if (chatModel!!.receiver_firebase_id == senderFirebaseId && chatModel.sender_firebase_id == receiverFirebaseId || chatModel.receiver_firebase_id == receiverFirebaseId && chatModel.sender_firebase_id == senderFirebaseId
//                    ) {
//                        chatModelList.add(chatModel)
//
//                    }
//                }
//                val layoutManager = LinearLayoutManager(this@ChatActivity)
//                layoutManager.stackFromEnd = true
//                binding.chatRc.layoutManager = layoutManager
//
//                messageAdapter =
//                    MessageAdapter(
//                        this@ChatActivity,
//                        chatModelList,
//                        sharePref.readString(Constants.USER_FIREBASE_ID, "").toString()
//                    )
//                binding.chatRc.adapter = messageAdapter
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//
//            }
//        }
//        )
//    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}