package com.example.ulaugh.controller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.ulaugh.R
import com.example.ulaugh.adapter.PostsAdapter
import com.example.ulaugh.databinding.FragmentProfileBinding
import com.example.ulaugh.model.HomeRecyclerViewItem
import com.example.ulaugh.model.PostItem
import com.example.ulaugh.model.UserRequest
import com.example.ulaugh.utils.Constants
import com.example.ulaugh.utils.SharePref
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProfileDetailActivity : AppCompatActivity() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var profileData: UserRequest? = null
    private val postItemsList: MutableList<PostItem> = mutableListOf()

//    private val authViewModel by activityViewModels<AuthViewModel>()

    @Inject
    lateinit var sharePref: SharePref
    private lateinit var allPostRef: DatabaseReference
    private lateinit var profileRef: DatabaseReference

    lateinit var postsAdapter: PostsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = FragmentProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        clickHandle()
        createBlurImage()
        CoroutineScope(Dispatchers.IO).launch {
            getPostData()
            getProfileData()
        }
    }

    private fun initViews() {
        allPostRef = FirebaseDatabase.getInstance().reference.child(Constants.POST_SHARE_REF)
        profileRef = FirebaseDatabase.getInstance().reference.child(Constants.USERS_REF)

        binding.rv.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        postsAdapter = PostsAdapter(this, postItemsList)
        binding.rv.adapter = postsAdapter
        binding.backBtn.visibility = View.VISIBLE
    }

    private fun clickHandle() {
        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun getProfileData() {
        profileRef.child(Firebase.auth.currentUser!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    profileData = snapshot.getValue(UserRequest::class.java)
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@ProfileDetailActivity,
                        "Canceled: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            })
    }

    private fun getPostData() {
        binding.progressBar.visibility = View.VISIBLE
        allPostRef.child(Firebase.auth.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (postSnap in snapshot.children) {
                        postItemsList.add(postSnap.getValue(PostItem::class.java)!!)
                    }
                    binding.progressBar.visibility = View.GONE
                    postsAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@ProfileDetailActivity,
                        "Canceled: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            })
    }

    private fun createBlurImage() {
        //Get seekBar progress
        Glide.with(this).load(R.drawable.seokangjoon)
            .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 4)))
            .into(binding.coverIv)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}