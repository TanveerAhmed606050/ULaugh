package com.example.ulaugh.controller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.ulaugh.R
import com.example.ulaugh.adapter.MessageAdapter
import com.example.ulaugh.databinding.ActivityChatBinding
import com.example.ulaugh.model.ChatModel
import com.example.ulaugh.utils.Constants
import com.example.ulaugh.utils.FirebaseDataBaseHashmapFunctionClass
import com.example.ulaugh.utils.Helper
import com.example.ulaugh.utils.SharePref
import com.google.firebase.auth.FirebaseAuth.getInstance
import com.google.firebase.database.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {

    @Inject
    lateinit var sharePref: SharePref
    private var _binding: ActivityChatBinding? = null
    private val binding get() = _binding!!

    var chatModelList: MutableList<ChatModel> = ArrayList()
    lateinit var messageAdapter: MessageAdapter
    var senderFirebaseId = ""
    var receiverFirebaseId = ""
    var senderPic = ""
    var messageId = ""
    private var receiverName = ""
    private var receiverPic = ""
    var senderName = ""
    private val firebaseDataBaseHashmapFunctionClass = FirebaseDataBaseHashmapFunctionClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        clickListener()
        initView()
        setAdapter()
    }

    private fun initView() {
        senderFirebaseId = getInstance().currentUser!!.uid
        if (intent != null) {
//            profileDetail =
//                Gson().fromJson(
//                    intent.getStringExtra(Constants.PROFILE),
//                    object : TypeToken<UserRequest>() {}.type
//                )
//        if (profileDetail != null) {
            receiverFirebaseId = intent.getStringExtra(Constants.FIREBASE_ID).toString()
            messageId = intent.getStringExtra("conversationId").toString()
            receiverPic = intent.getStringExtra(Constants.PROFILE_PIC).toString()
            receiverName = intent.getStringExtra("receiverName").toString()
            binding.nameTv.text = receiverName
            Glide.with(this)
                .load(receiverPic)
                .centerCrop()
                .fitCenter()
                .thumbnail()
                .placeholder(R.drawable.user_logo)
                .into(binding.profileIv)
//            receiverFirebaseId = profileDetail!!.firebase_id!!
//            messageId = "$senderFirebaseId + $receiverFirebaseId"
//            receiverName = profileDetail!!.full_name!!
            senderPic = sharePref.readString(Constants.PROFILE_PIC, "").toString()
            senderName = sharePref.readString(Constants.USER_NAME, "").toString()
        }

        val isBool = intent.extras?.getBoolean(Constants.IS_CHECKED) == true
        if (!isBool) {
            readMessagesFromAllConversation(messageId)
        } else {
            messageId = "${senderFirebaseId}+${receiverFirebaseId}"
            FirebaseDatabase.getInstance()
                .getReference("All_Conversations")
                .child(messageId)
                .child("messages").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            messageId = "${senderFirebaseId}+${receiverFirebaseId}"
                            readMessagesFromAllConversation(messageId)
                        } else {
                            messageId = "${receiverFirebaseId}+${senderFirebaseId}"
                            readMessagesFromAllConversation(messageId)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                    }
                })
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
            sendMessageFun(
                senderFirebaseId,
                receiverFirebaseId,
                message,
                time,
                messageType
            )
        }
    }


    private fun sendMessageFun(
        senderFirebaseId: String,
        receiverFirebaseId: String,
        message: String,
        dateAndTime: String,
        messageType: String
    ) {

        val senderConversionHashmap =
            firebaseDataBaseHashmapFunctionClass.inboxConversationHashmapFun(
                messageId,
                receiverFirebaseId,
                receiverName,
                dateAndTime,
                message,
                messageType,
                receiverPic
            )
        val receiverConversionHashmap =
            firebaseDataBaseHashmapFunctionClass.inboxConversationHashmapFun(
                messageId,
                senderFirebaseId,
                senderName,
                dateAndTime,
                message,
                messageType,
                senderPic
            )

        val senderDatabaseReference =
            FirebaseDatabase.getInstance().getReference("Inbox").child(senderFirebaseId)
                .child("conversations").child(messageId)
        senderDatabaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                senderDatabaseReference.setValue(senderConversionHashmap)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        val receiverDatabaseReference =
            FirebaseDatabase.getInstance().getReference("Inbox").child(receiverFirebaseId)
                .child("conversations").child(messageId)

        receiverDatabaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                receiverDatabaseReference.setValue(receiverConversionHashmap)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

        val messageHashmap = firebaseDataBaseHashmapFunctionClass.messageHashmapFun(
            messageId,
            messageType,
            message,
            dateAndTime,
            senderFirebaseId,
            senderName,
            receiverFirebaseId,
            receiverName,
        )
        val messageReference =
            FirebaseDatabase.getInstance()
                .getReference("All_Conversations")
                .child(messageId)
                .child("messages")

        messageReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messageReference.push().setValue(messageHashmap)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
        readMessagesFromAllConversation(messageId)
    }

    private fun readMessagesFromAllConversation(messageId: String) {
        val messageReference = FirebaseDatabase.getInstance().getReference("All_Conversations")
            .child(messageId)
            .child("messages")
        messageReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatModelList.clear()
                for (ds in snapshot.children) {
                    val chatModel: ChatModel? = ds.getValue(ChatModel::class.java)
                    if (chatModel!!.receiver_firebase_id == senderFirebaseId && chatModel.sender_firebase_id == receiverFirebaseId || chatModel.receiver_firebase_id == receiverFirebaseId && chatModel.sender_firebase_id == senderFirebaseId) {
                        chatModelList.add(chatModel)
                    }
                }
                messageAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun setAdapter() {
        val layoutManager = LinearLayoutManager(this@ChatActivity)
        layoutManager.stackFromEnd = true
        binding.chatRc.layoutManager = layoutManager

        messageAdapter =
            MessageAdapter(
                this@ChatActivity,
                chatModelList,
                receiverFirebaseId,
                senderFirebaseId,
                senderPic,
                receiverPic
            )
        binding.chatRc.adapter = messageAdapter
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}