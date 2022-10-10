package com.example.ulaugh.controller

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.ulaugh.R
import com.example.ulaugh.adapter.PostsAdapter
import com.example.ulaugh.databinding.ActivityProfileDetailBinding
import com.example.ulaugh.interfaces.PostClickListener
import com.example.ulaugh.model.*
import com.example.ulaugh.utils.Constants
import com.example.ulaugh.utils.SharePref
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.*
import javax.inject.Inject
import android.Manifest
import com.example.ulaugh.api.RetrofitInstance
import com.example.ulaugh.utils.Constants.TAG
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

const val TOPIC = "/topics/myTopic2"
@AndroidEntryPoint
class ProfileDetailActivity : AppCompatActivity(), PostClickListener {
    private var _binding: ActivityProfileDetailBinding? = null
    private val binding get() = _binding!!
    private var profileData: UserRequest? = null
    private val postItemsList: MutableList<HomeRecyclerViewItem.SharePostData> = mutableListOf()
    private var firebaseId = ""
    private var isFollow = false
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
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val window: Window = window
            window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }
        initViews()
        clickHandle()
        createBlurImage()
        CoroutineScope(Dispatchers.IO).launch {
//            binding.progressBar.visibility = View.VISIBLE
            getProfileData()
//            delay(1000)

//            binding.progressBar.visibility = View.GONE
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
        isFollow = intent.getBooleanExtra(Constants.IS_FOLLOW, false)
        allPostRef = FirebaseDatabase.getInstance().reference.child(Constants.POST_SHARE_REF)
        profileRef = FirebaseDatabase.getInstance().reference.child(Constants.USERS_REF)
        if (isFollow)
            binding.followBtn.text = "Message"

        binding.rv.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        postsAdapter = PostsAdapter(this, postItemsList, this)
        binding.rv.adapter = postsAdapter
//        binding.backBtn.visibility = View.VISIBLE
    }

    private fun setProfileData(isPrivate: Boolean) {
        binding.nameTv.text = profileData!!.full_name
        binding.idTv.text = profileData!!.user_name
        if (isPrivate) {
            binding.rv.visibility = View.GONE
            binding.lockLogo.visibility = View.VISIBLE
            binding.textView20.visibility = View.VISIBLE
            binding.textView21.visibility = View.VISIBLE
            binding.followBtn.text = "Request"
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
            .placeholder(R.drawable.user_logo)
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
        binding.backBtn.setOnClickListener {
            finish()
        }
        binding.followBtn.setOnClickListener {
            if (isFollow) {
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra(Constants.FIREBASE_ID, profileData!!.firebase_id)
                intent.putExtra("IsChecked", true)
                intent.putExtra("receiverName", profileData!!.full_name)
                intent.putExtra(Constants.PROFILE_PIC, profileData!!.profile_pic)
//                intent.putExtra(Constants.PROFILE, Gson().toJson(profileData))
                intent.putExtra(Constants.IS_CHECKED, true)
                startActivity(intent)
            } else {
                askNotificationPermission()
            }
        }
    }

    private fun getProfileData() {
        profileRef.child(firebaseId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
//                    Log.d(TAG, "isPrivate: ${snapshot.child("is_private").value}")
                    val isPrivate = snapshot.child("is_private").value as Boolean
                    profileData = snapshot.getValue(UserRequest::class.java)
                    setProfileData(isPrivate)
                    if (isPrivate)
                        getPostData()
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
                        val keyValue = postSnap.key.toString()
                        var userReaction = ""
                        val reactionsList: MutableList<Reactions> = ArrayList()

                        for (reactionItem in postSnap.child(Constants.REACTION).children) {
                            val reactions = reactionItem.getValue(Reactions::class.java)!!
                            if (reactions.user_id == FirebaseAuth.getInstance().currentUser!!.uid)
                                userReaction = reactions.reaction_type!!
                            else
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
                            reactionsList, userReaction
                        )
                        Log.d(Constants.TAG, "onDataChange: ${userReaction}\n")
                        postItemsList.add(postItem)
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

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.DONUT) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_NOTIFICATION_POLICY
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_NOTIFICATION_POLICY)) {

            } else {
                // Directly ask for the permission
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
            // FCM SDK (and your app) can post notifications.
        } else {
            Toast.makeText(this, "Please enable notifications", Toast.LENGTH_SHORT).show()
        }
    }

    fun createNotification() {
        val title = "Request"
        val message = "Friend request received from"
        val recipientToken = "etToken.text.toString()"
        if(title.isNotEmpty() && message.isNotEmpty() && recipientToken.isNotEmpty()) {
            PushNotification(
                NotificationData(title, message),
                recipientToken
            ).also {
                sendNotification(it)
            }
        }
        // [END fcm_send_upstream]
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if(response.isSuccessful) {
                Log.d(TAG, "Response: ${Gson().toJson(response)}")
            } else {
                Log.e(TAG, response.errorBody().toString())
            }
        } catch(e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

//    fun fetchToken() {
//        // [START fcm_runtime_enable_auto_init]
//        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
//            if (!task.isSuccessful) {
//                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
//                return@OnCompleteListener
//            }
//
//            // Get new FCM registration token
//            val token = task.result
//
//            // Log and toast
//            val msg = getString(R.string.msg_token_fmt, token)
//            Log.d(TAG, msg)
//            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
//        })
//        // [END fcm_runtime_enable_auto_init]
//    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


    override fun onClick(post: Any, type: String, emotionList: List<Emoji>?) {
    }
}