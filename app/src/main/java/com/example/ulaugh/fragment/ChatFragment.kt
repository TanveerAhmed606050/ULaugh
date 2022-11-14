package com.example.ulaugh.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ulaugh.adapter.InboxConversationAdapter
import com.example.ulaugh.databinding.FragmentChatBinding
import com.example.ulaugh.model.ChatModel
import com.example.ulaugh.model.InboxListModel
import com.example.ulaugh.utils.SharePref
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChatFragment : Fragment() {
    private var senderFirebaseId = ""
    var userList: MutableList<InboxListModel> = ArrayList()
    lateinit var mAdapter: InboxConversationAdapter
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private var conversationId = ""
    var receiverFirebaseId = ""
    var allFriendsChat: ArrayList<ArrayList<ChatModel>> = ArrayList()
    var inboxChatModel: InboxListModel? = null

    @Inject
    lateinit var sharePref: SharePref
    lateinit var authFirebase: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        senderFirebaseId = Firebase.auth.currentUser!!.uid

        val job1 = CoroutineScope(Dispatchers.Main).launch {
            getAllConversationAdapterFun()
            Log.d("kdshga", "job1: ")
        }
//        CoroutineScope(Dispatchers.Main).launch {
//            job1.join()
//            for (conversationId in conversationIdList) {
//            }
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun setAdapter() {
        val layoutManager1 = LinearLayoutManager(requireContext())
        binding.chatRv.layoutManager = layoutManager1
        mAdapter = InboxConversationAdapter(requireContext(), userList)
        binding.chatRv.adapter = mAdapter
    }

    private fun getAllConversationAdapterFun() {
        var position = 0
        val receiverDatabaseReference =
            FirebaseDatabase.getInstance().getReference("Inbox").child(senderFirebaseId)
                .child("conversations")
        receiverDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    userList.clear()
                    try {
                        for (ds in snapshot.children) {
                            inboxChatModel =
                                ds.getValue(InboxListModel::class.java)
                            userList.add(inboxChatModel!!)
                            if (userList.isEmpty()) {
                                binding.noData.visibility = View.VISIBLE
                            } else {
                                binding.noData.visibility = View.GONE
                            }
                            receiverFirebaseId =
                                inboxChatModel!!.conversation_id.replace(senderFirebaseId, "")
                                    .replace("+", "").trim()
//                            conversationId = "${senderFirebaseId}+${receiverFirebaseId}"
//                            checkConversation(conversationId, position)
                            position++
                        }
                        setAdapter()
                    } catch (e: NullPointerException) {
                        println()
                    }
                } else
                    binding.noData.visibility = View.VISIBLE
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

//    private fun checkConversation(conversationId: String, position: Int) {
//        FirebaseDatabase.getInstance()
//            .getReference("All_Conversations")
//            .child(conversationId)
//            .child("messages").addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    if (snapshot.exists()) {
//                        readMessagesFromAllConversation(conversationId, position)
//                        Log.d("kdshga", "job2: $conversationId")
//                    } else {
//                        val conversationIds = "${receiverFirebaseId}+${senderFirebaseId}"
//                        readMessagesFromAllConversation(conversationIds, position)
//                        Log.d("kdshga", "job2: $conversationIds")
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                }
//            })
//    }

//    private fun readMessagesFromAllConversation(conversationId: String, position: Int) {
//        var count = 0
//        var chatModelList = ArrayList<ChatModel>()
//        val messageReference = FirebaseDatabase.getInstance().getReference("All_Conversations")
//            .child(conversationId)
//        messageReference.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                chatModelList.clear()
//                for (ds in snapshot.children) {
//                    val chatModel: ChatModel? = ds.getValue(ChatModel::class.java)
//                    if (chatModel!!.receiver_firebase_id == senderFirebaseId && chatModel.sender_firebase_id == receiverFirebaseId || chatModel.receiver_firebase_id == receiverFirebaseId && chatModel.sender_firebase_id == senderFirebaseId) {
//                        chatModelList.add(chatModel)
//                    }
//                    if (chatModel.receiver_firebase_id == senderFirebaseId && !chatModel.seen_message)
//                        count++
//                    Log.d(
//                        "skdhg",
//                        "${chatModelList} ${chatModel!!.seen_message} $count ${chatModel.receiver_firebase_id} $senderFirebaseId"
//                    )
//                    allFriendsChat.add(chatModelList)
//                }
//                val chatModel = userList[position]
//                chatModel.unReadMsg = count
//                userList[position] = chatModel
//                mAdapter.notifyDataSetChanged()
////                setAdapter()
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//            }
//        })
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}