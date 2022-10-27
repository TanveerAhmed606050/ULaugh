package com.example.ulaugh.controller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.example.ulaugh.R
import com.example.ulaugh.adapter.RewardAdapter
import com.example.ulaugh.databinding.ActivityRewardBinding
import com.example.ulaugh.model.HomeRecyclerViewItem
import com.example.ulaugh.model.PostItem
import com.example.ulaugh.model.Reactions
import com.example.ulaugh.utils.Constants
import com.example.ulaugh.utils.SharePref
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class RewardActivity : AppCompatActivity() {
    private var _binding: ActivityRewardBinding? = null
    private val binding get() = _binding!!
    private var postShareRef: DatabaseReference? = null
    private var postList: ArrayList<HomeRecyclerViewItem.SharePostData> = ArrayList()

    @Inject
    lateinit var tokenManager: SharePref
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRewardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initValue()
        setAdapter()
        CoroutineScope(Dispatchers.IO).launch {
            getPostsData()
        }
    }

    private fun setAdapter() {
        val adapter = RewardAdapter(this, postList)
        binding.rv.adapter = adapter
    }

    private fun initValue() {
        postShareRef = FirebaseDatabase.getInstance().getReference(Constants.POST_SHARE_REF)
        binding.included.headerTitle.visibility = View.VISIBLE
        binding.included.headerTitle.text = getText(R.string.reward)
        binding.included.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun getPostsData() {
        postShareRef!!.child(FirebaseAuth.getInstance().currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChildren()) {
                        for (postSnap in snapshot.children) {
                            val keyValue = postSnap.key.toString()
                            val reactionsList: MutableList<Reactions> = ArrayList()

                            for (reactionItem in postSnap.child(Constants.REACTION).children) {
                                val reactions = reactionItem.getValue(Reactions::class.java)!!
                                reactionsList.add(reactions)
                            }
                            val post = postSnap.getValue(PostItem::class.java)
                            post!!.post_id = keyValue
                            val postItem = HomeRecyclerViewItem.SharePostData(
                                keyValue,
                                post.firebase_id,
                                post.image_url,
                                post.description,
                                post.date_time,
                                post.user_name,
                                post.full_name,
                                post.tagsList,
                                post.profile_image,
                                reactionsList,
                                post._profile_pic
                            )
                            postList.add(postItem)
                        }
                        binding.rv.adapter!!.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}