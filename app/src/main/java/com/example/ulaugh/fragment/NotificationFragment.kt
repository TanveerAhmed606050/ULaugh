package com.example.ulaugh.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ulaugh.adapter.NotificationAdapter
import com.example.ulaugh.databinding.FragmentNotificationBinding
import com.example.ulaugh.interfaces.NotificationListener
import com.example.ulaugh.model.Friend
import com.example.ulaugh.model.Notification
import com.example.ulaugh.utils.Constants
import com.example.ulaugh.utils.SharePref
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationFragment : Fragment(), NotificationListener {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    private var notificationRef: DatabaseReference? = null
    private lateinit var userRef: DatabaseReference

    @Inject
    lateinit var tokenManager: SharePref
    private var notificationList: ArrayList<Notification> = ArrayList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setAdapter()
        CoroutineScope(Dispatchers.IO).launch {
            getNotifications()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun initViews() {
        userRef = FirebaseDatabase.getInstance().getReference(Constants.USERS_REF)
        notificationRef = FirebaseDatabase.getInstance().getReference(Constants.NOTIFICATION)
    }

    private fun setAdapter() {
        val layoutManager = LinearLayoutManager(context)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        binding.notificationRv.layoutManager = layoutManager
        val adapter = NotificationAdapter(notificationList, requireContext(), this)
        binding.notificationRv.adapter = adapter
    }

    private fun getNotifications() {
        notificationRef!!.child(FirebaseAuth.getInstance().currentUser!!.uid).orderByValue()
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (notificationItem in snapshot.children) {
                        val notification = notificationItem.getValue(Notification::class.java)
                        notification!!.notificationId = notificationItem.key!!
                        notificationList.add(notification)
                    }
                    if (notificationList.isNotEmpty())
                        setAdapter()
                    else
                        binding.noData.visibility = View.VISIBLE
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onNotification(notification: Notification, message: String) {
        if (message == Constants.ACCEPT) {
            userRef.child(notification.senderId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val selfId = Friend(notification.senderId, false) // isFollow
                        userRef.child(FirebaseAuth.getInstance().currentUser!!.uid)
                            .child(Constants.FRIENDS_REF).child(notification.senderId)
                            .setValue(selfId)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            requireContext(),
                            "Error: ${error.message}",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                })
        }
        notificationRef!!.child(FirebaseAuth.getInstance().currentUser!!.uid)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    notification.title = message
                    notification.description = "$message your friend request"
                    notificationRef!!.child(FirebaseAuth.getInstance().currentUser!!.uid)
                        .child(notification.notificationId).setValue(notification)
                    notificationList.clear()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun notificationSeen() {
        notificationRef!!.child(FirebaseAuth.getInstance().currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (notificationSnap in snapshot.children) {
                        val notificationItem = notificationSnap.getValue(Notification::class.java)
//                        if (!notificationItem!!.seen!!)
//                            return
                        notificationItem!!.seen = true
                        notificationRef!!.child(FirebaseAuth.getInstance().currentUser!!.uid)
                            .child(notificationItem.notificationId).setValue(notificationItem)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onPause() {
        super.onPause()
//        CoroutineScope(Dispatchers.IO).launch {
//            notificationSeen()
//        }
    }
}