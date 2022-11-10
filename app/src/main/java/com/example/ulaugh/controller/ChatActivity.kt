package com.example.ulaugh.controller

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.ulaugh.R
import com.example.ulaugh.adapter.MessageAdapter
import com.example.ulaugh.api.RetrofitInstance
import com.example.ulaugh.databinding.ActivityChatBinding
import com.example.ulaugh.model.*
import com.example.ulaugh.utils.Constants
import com.example.ulaugh.utils.FirebaseDataBaseHashmapFunctionClass
import com.example.ulaugh.utils.Helper
import com.example.ulaugh.utils.SharePref
import com.google.firebase.auth.FirebaseAuth.getInstance
import com.google.firebase.database.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    var conversationId = ""
    private var receiverName = ""
    private var receiverPic = ""
    var senderName = ""
    private val firebaseDataBaseHashmapFunctionClass = FirebaseDataBaseHashmapFunctionClass()
    private lateinit var notificationRef: DatabaseReference
    private var receiverMessageToken = ""
    private var senderMessageToken = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        clickListener()
        initView()
        setAdapter()
    }

    private fun initView() {
        senderMessageToken = sharePref.readString(Constants.MESSAGE_TOKEN, "")!!
        senderFirebaseId = getInstance().currentUser!!.uid
        notificationRef = FirebaseDatabase.getInstance().getReference(Constants.NOTIFICATION)
        if (intent != null) {
//            profileDetail =
//                Gson().fromJson(
//                    intent.getStringExtra(Constants.PROFILE),
//                    object : TypeToken<UserRequest>() {}.type
//                )
//        if (profileDetail != null) {
            receiverFirebaseId = intent.getStringExtra(Constants.FIREBASE_ID).toString()
            conversationId = intent.getStringExtra("conversationId").toString()
            receiverPic = intent.getStringExtra(Constants.PROFILE_PIC).toString()
            receiverMessageToken = intent.getStringExtra(Constants.MESSAGE_TOKEN).toString()
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
            readMessagesFromAllConversation(conversationId)
        } else {
            conversationId = "${senderFirebaseId}+${receiverFirebaseId}"
            FirebaseDatabase.getInstance()
                .getReference("All_Conversations")
                .child(conversationId)
                .child("messages").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            conversationId = "${senderFirebaseId}+${receiverFirebaseId}"
                            readMessagesFromAllConversation(conversationId)
                        } else {
                            conversationId = "${receiverFirebaseId}+${senderFirebaseId}"
                            readMessagesFromAllConversation(conversationId)
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

//    private fun getFriendProfile() {
//        profileRef.child(friendFirebaseId)
//            .addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
////                    Log.d(TAG, "isPrivate: ${snapshot.child("is_private").value}")
//                    isPrivate = snapshot.child(Constants.IS_PRIVATE).value as Boolean
//                    messageToken = snapshot.child(Constants.MESSAGE_TOKEN).value.toString()
//                    profileData = snapshot.getValue(UserRequest::class.java)
//                    setProfileData(isPrivate)
////                    if (!isPrivate)
//                    getPostData()
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    Toast.makeText(
//                        this@ProfileDetailActivity,
//                        "Canceled: ${error.message}",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            })
//        profileRef.child(FirebaseAuth.getInstance().currentUser!!.uid).child(Constants.FRIENDS_REF)
//            .child(friendFirebaseId).addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    isFollow = snapshot.child(Constants.IS_FOLLOW).value.toString().toBoolean()
//                    if (isFollow)
//                        binding.followBtn.text = "Message"
//
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                }
//
//            })
////        return profileData
//    }

    private fun sendMessageFun(
        senderFirebaseId: String,
        receiverFirebaseId: String,
        message: String,
        dateAndTime: String,
        messageType: String
    ) {
        val senderConversionHashmap =
            firebaseDataBaseHashmapFunctionClass.inboxConversationHashmapFun(
                conversationId,
                receiverFirebaseId,
                receiverName,
                dateAndTime,
                message,
                messageType,
                receiverPic,
                senderMessageToken
            )
        val receiverConversionHashmap =
            firebaseDataBaseHashmapFunctionClass.inboxConversationHashmapFun(
                conversationId,
                senderFirebaseId,
                senderName,
                dateAndTime,
                message,
                messageType,
                senderPic,
                receiverMessageToken
            )

        val senderDatabaseReference =
            FirebaseDatabase.getInstance().getReference("Inbox").child(senderFirebaseId)
                .child("conversations").child(conversationId)
        senderDatabaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                senderDatabaseReference.setValue(senderConversionHashmap)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

        val receiverDatabaseReference =
            FirebaseDatabase.getInstance().getReference("Inbox").child(receiverFirebaseId)
                .child("conversations").child(conversationId)

        receiverDatabaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                receiverDatabaseReference.setValue(receiverConversionHashmap)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
        val messageHashmap = firebaseDataBaseHashmapFunctionClass.messageHashmapFun(
            conversationId,
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
                .child(conversationId)
                .child("messages")

        messageReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messageReference.push().setValue(messageHashmap)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
        readMessagesFromAllConversation(conversationId)
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

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.DONUT) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_NOTIFICATION_POLICY
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                createNotification()
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_NOTIFICATION_POLICY)) {
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_NOTIFICATION_POLICY)
            }
        }
    }
    // [START ask_post_notifications]
    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            createNotification()
            // FCM SDK (and your app) can post notifications.
        } else {
            Toast.makeText(this, "Please enable notifications", Toast.LENGTH_SHORT).show()
        }
    }
    private fun createNotification() {
        val title = "Message"
        val message =
            "${sharePref.readString(Constants.FULL_NAME, "")} Sent you a message"
        val recipientToken = receiverMessageToken
//        val recipientToken = "cfNtuWZzTVGBYI7CFXuKq6:APA91bFYbHB64p9vkfyj6U3_Ii8YLDFnqUTja4q9uyNtk6GrwIqWi7L-RmU-AJI_nrH__gZsFkOVEw4uflzaOzifIpuA_1XDgHGGgeqDjJztbZpEd1k6QTgZ4rAp_j9CRradIqQ9MTsB"
        if (title.isNotEmpty() && message.isNotEmpty() && recipientToken.isNotEmpty()) {
            PushNotification(
                NotificationData(title, message),
                recipientToken
            ).also {
                sendNotification(it)
            }
        }
        // [END fcm_send_upstream]
    }
    private fun sendNotification(notification: PushNotification) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = RetrofitInstance.api.postNotification(notification)
                if (response.isSuccessful) {
                    Log.d(Constants.TAG, "Response: $response")
                } else {
                    Log.e(Constants.TAG, response.errorBody().toString())
                }
            } catch (e: Exception) {
                Log.e(Constants.TAG, e.toString())
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}