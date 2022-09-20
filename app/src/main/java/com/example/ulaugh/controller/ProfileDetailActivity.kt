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
import com.example.ulaugh.databinding.ActivityProfileDetailBinding
import com.example.ulaugh.interfaces.PostClickListener
import com.example.ulaugh.model.PostItem
import com.example.ulaugh.model.UserRequest
import com.example.ulaugh.utils.Constants
import com.example.ulaugh.utils.SharePref
import com.google.firebase.database.*
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class ProfileDetailActivity : AppCompatActivity(), PostClickListener {
    private var _binding: ActivityProfileDetailBinding? = null
    private val binding get() = _binding!!
    private var profileData: UserRequest? = null
    private val postItemsList: MutableList<PostItem> = mutableListOf()
    private var firebaseId = ""
//    private val authViewModel by activityViewModels<AuthViewModel>()

    @Inject
    lateinit var sharePref: SharePref
    private lateinit var allPostRef: DatabaseReference
    private lateinit var profileRef: DatabaseReference

    lateinit var postsAdapter: PostsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityProfileDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        clickHandle()
        createBlurImage()
        CoroutineScope(Dispatchers.IO).launch {
            binding.progressBar.visibility = View.VISIBLE
            getProfileData()
            getPostData()
            binding.progressBar.visibility = View.GONE
        }
//        runBlocking {
//            job.join()
//            if (!profileData!!.is_private)
//                getPostData()
//            setProfileData()
//            job.cancel()
//        }
    }

    private fun initViews() {
        firebaseId = intent.getStringExtra(Constants.FIREBASE_ID)!!
        allPostRef = FirebaseDatabase.getInstance().reference.child(Constants.POST_SHARE_REF)
        profileRef = FirebaseDatabase.getInstance().reference.child(Constants.USERS_REF)

        binding.rv.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        postsAdapter = PostsAdapter(this, postItemsList, this)
        binding.rv.adapter = postsAdapter
//        binding.backBtn.visibility = View.VISIBLE
    }

    private fun setProfileData() {
        binding.nameTv.text = profileData!!.full_name
        binding.idTv.text = profileData!!.user_name
        if (profileData!!.is_private) {
            binding.rv.visibility = View.GONE
            binding.lockLogo.visibility = View.VISIBLE
            binding.textView20.visibility = View.VISIBLE
            binding.textView21.visibility = View.VISIBLE
        } else {
            binding.rv.visibility = View.VISIBLE
            binding.lockLogo.visibility = View.GONE
            binding.textView20.visibility = View.GONE
            binding.textView21.visibility = View.GONE

        }
        Glide.with(this)
            .load(profileData!!.profile_pic)
            .centerCrop()
            .fitCenter()
            .thumbnail()
            .placeholder(R.drawable.seokangjoon)
            .into(binding.profileIv)
        Glide.with(this)
            .load(profileData!!.profile_pic)
            .centerCrop()
            .fitCenter()
            .thumbnail()
            .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 4)))
            .placeholder(R.drawable.seokangjoon)
            .into(binding.coverIv)
    }

    private fun clickHandle() {
//        binding.backBtn.setOnClickListener {
//            finish()
//        }
    }

    private fun getProfileData() {
        profileRef.child(firebaseId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    profileData = snapshot.getValue(UserRequest::class.java)
                    setProfileData()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@ProfileDetailActivity,
                        "Canceled: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
//        return profileData
    }

    private fun getPostData() {
        allPostRef.child(firebaseId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (postSnap in snapshot.children) {
                        postItemsList.add(postSnap.getValue(PostItem::class.java)!!)
                    }
                    binding.postTv.text = "${postItemsList.size}"
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

    override fun onClick(post: Any, type: String) {

    }
}