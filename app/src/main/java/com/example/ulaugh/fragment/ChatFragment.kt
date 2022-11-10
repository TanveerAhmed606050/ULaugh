package com.example.ulaugh.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ulaugh.adapter.InboxConversationAdapter
import com.example.ulaugh.databinding.FragmentChatBinding
import com.example.ulaugh.model.InboxListModel
import com.example.ulaugh.utils.SharePref
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChatFragment : Fragment() {
    private var firebaseId = ""
    var userList: MutableList<InboxListModel> = ArrayList()
    lateinit var mAdapter: InboxConversationAdapter
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var sharePref: SharePref
    lateinit var authFirebase: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseId = Firebase.auth.currentUser!!.uid
//        CoroutineScope(Dispatchers.IO).launch {
        getAllConversationAdapterFun()
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

    private fun getAllConversationAdapterFun() {
        val receiverDatabaseReference =
            FirebaseDatabase.getInstance().getReference("Inbox").child(firebaseId)
                .child("conversations")
        receiverDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    userList.clear()
                    try {
                        for (ds in snapshot.children) {
                            val inboxChatModel: InboxListModel? =
                                ds.getValue(InboxListModel::class.java)
                            userList.add(inboxChatModel!!)
                            if (userList.isEmpty()) {
                                binding.noData.visibility = View.VISIBLE
                            } else {
                                binding.noData.visibility = View.GONE
                            }
                        }
                        val layoutManager1 = LinearLayoutManager(requireContext())
                        binding.chatRv.layoutManager = layoutManager1
                        mAdapter = InboxConversationAdapter(requireContext(), userList)
                        binding.chatRv.adapter = mAdapter
                    } catch (e: NullPointerException) {
                        println()
                    }
                } else
                    binding.noData.visibility = View.VISIBLE
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}