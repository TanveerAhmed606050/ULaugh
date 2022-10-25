package com.example.ulaugh.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ulaugh.adapter.NotificationAdapter
import com.example.ulaugh.databinding.FragmentNotificationBinding
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
class NotificationFragment : Fragment() {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    private var notificationRef: DatabaseReference? = null

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
        notificationRef = FirebaseDatabase.getInstance().getReference(Constants.NOTIFICATION)
    }

    private fun setAdapter() {
        val layoutManager = LinearLayoutManager(context)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        binding.notificationRv.layoutManager = layoutManager
        val adapter = NotificationAdapter(notificationList, requireContext())
        binding.notificationRv.adapter = adapter
    }

    private fun getNotifications() {

        notificationRef!!.child(FirebaseAuth.getInstance().currentUser!!.uid).orderByValue()
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (notificationItem in snapshot.children) {
                        val notification = notificationItem.getValue(Notification::class.java)
                        notificationList.add(notification!!)
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

}