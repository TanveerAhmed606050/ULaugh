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
import com.example.ulaugh.interfaces.AddFriendListener
import com.example.ulaugh.model.*
import com.example.ulaugh.utils.Constants
import com.example.ulaugh.utils.SharePref
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.*
import javax.inject.Inject
import android.Manifest
import android.content.Context
import com.example.ulaugh.api.RetrofitInstance
import com.example.ulaugh.utils.Constants.TAG
import com.example.ulaugh.utils.Helper
import com.google.firebase.messaging.FirebaseMessaging

const val TOPIC = "/topics/myTopic2"

@AndroidEntryPoint
class ProfileDetailActivity : AppCompatActivity(), AddFriendListener {
    private var _binding: ActivityProfileDetailBinding? = null
    private val binding get() = _binding!!
    private var profileData: UserRequest? = null
    private val postItemsList: MutableList<HomeRecyclerViewItem.SharePostData> = mutableListOf()
    private var friendFirebaseId = ""
    private var isFollow = false
//    private val authViewModel by activityViewModels<AuthViewModel>()

    @Inject
    lateinit var sharePref: SharePref
    private lateinit var allPostRef: DatabaseReference
    private lateinit var profileRef: DatabaseReference
    private lateinit var notificationRef: DatabaseReference
    private var isPrivate: Boolean = false
    private var messageToken = ""

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
//            fetchToken()
            getProfileData()
            getFollowers()
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
        friendFirebaseId = intent.getStringExtra(Constants.FIREBASE_ID)!!
//        isFollow = intent.getBooleanExtra(Constants.IS_FOLLOW, false)
        allPostRef = FirebaseDatabase.getInstance().reference.child(Constants.POST_SHARE_REF)
        profileRef = FirebaseDatabase.getInstance().reference.child(Constants.USERS_REF)
        notificationRef = FirebaseDatabase.getInstance().getReference(Constants.NOTIFICATION)

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
            if (isPrivate) {
                askNotificationPermission()
                CoroutineScope(Dispatchers.IO).launch {
                    postNotification(profileData!!.firebase_id!!)
                }
            } else if (isFollow) {
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra(Constants.FIREBASE_ID, profileData!!.firebase_id)
                intent.putExtra("IsChecked", true)
                intent.putExtra("receiverName", profileData!!.full_name)
                intent.putExtra(Constants.PROFILE_PIC, profileData!!.profile_pic)
//                intent.putExtra(Constants.PROFILE, Gson().toJson(profileData))
                intent.putExtra(Constants.IS_CHECKED, true)
                startActivity(intent)
            } else {
                profileRef.child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .child(Constants.FRIENDS_REF).child(friendFirebaseId).child(Constants.IS_FOLLOW)
                    .setValue(true).addOnSuccessListener {
                        Toast.makeText(this, "Follow Successfully", Toast.LENGTH_SHORT).show()
                        binding.followBtn.text = "Message"
                        isFollow = true
                    }
            }
        }
    }

    private fun getFollowers(): Int {
        var followerCount = 0
        profileRef.child(friendFirebaseId).child(Constants.FRIENDS_REF)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (friendSnap in snapshot.children) {
                        val friend = friendSnap.getValue(Friend::class.java)
//                        Log.d(TAG, "Inside for friends: $friend")
                        if (friend!!._follow!!) {
                            followerCount++
//                            Log.d(TAG, "Inside if: $followerCount")
                        }
                    }
//                    Log.d(TAG, "Outside for: $followerCount")
                    binding.followerTv.text = "$followerCount"
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@ProfileDetailActivity,
                        "Error: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            })
        return followerCount
    }

    private fun getProfileData() {
        profileRef.child(friendFirebaseId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
//                    Log.d(TAG, "isPrivate: ${snapshot.child("is_private").value}")
                    isPrivate = snapshot.child(Constants.IS_PRIVATE).value as Boolean
                    messageToken = snapshot.child(Constants.MESSAGE_TOKEN).value.toString()
                    profileData = snapshot.getValue(UserRequest::class.java)
                    setProfileData(isPrivate)
//                    if (!isPrivate)
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
        profileRef.child(FirebaseAuth.getInstance().currentUser!!.uid).child(Constants.FRIENDS_REF)
            .child(friendFirebaseId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isFollow = snapshot.child(Constants.IS_FOLLOW).value.toString().toBoolean()
                    if (isFollow)
                        binding.followBtn.text = "Message"

                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
//        return profileData
    }

    private fun getPostData() {
        allPostRef.child(friendFirebaseId)
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
                            reactionsList,
                            post._profile_pic,
                            userReaction
                        )
                        Log.d(TAG, "onDataChange: ${userReaction}\n")
                        postItemsList.add(postItem)
                        if (post._profile_pic == "true") {
                            val emotionsList = countReactions(reactionsList)
                            setEmotions(emotionsList, this@ProfileDetailActivity)
                        }
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

    private fun countReactions(reactionList: MutableList<Reactions>): List<Pair<String?, Int>> {
        val frequencies = reactionList.groupingBy { it.reaction_type }.eachCount()
        return frequencies.toList().sortedByDescending { (key, value) -> value }
    }

    private fun setEmotions(emotionsList: List<Pair<String?, Int>>, context: Context) {
        var position = 1 //set half emotions
        for (emotion in emotionsList) {
            when (position) {
                1 -> {
                    when (emotion.first) {
                        "happy" -> binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
                        "sad" -> binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                        "fear" -> binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                        "neutral" -> binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
                        "angry" -> binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
                        "surprise" -> binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                        "disgust" -> binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                    }
                    binding.reactIv1.visibility = View.VISIBLE
                }
                2 -> {
                    when (emotion.first) {
                        "happy" -> binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
                        "sad" -> binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                        "fear" -> binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                        "neutral" -> binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
                        "angry" -> binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
                        "surprise" -> binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                        "disgust" -> binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                    }
                    binding.reactIv2.visibility = View.VISIBLE
                }
                3 -> {
                    when (emotion.first) {
                        "happy" -> binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
                        "sad" -> binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                        "fear" -> binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                        "neutral" -> binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
                        "angry" -> binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
                        "surprise" -> binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                        "disgust" -> binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                    }
                    binding.reactIv3.visibility = View.VISIBLE
                }
                4 -> {
                    when (emotion.first) {
                        "happy" -> binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
                        "sad" -> binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                        "fear" -> binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                        "neutral" -> binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
                        "angry" -> binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
                        "surprise" -> binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                        "disgust" -> binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                    }
                    binding.reactIv4.visibility = View.VISIBLE
                }
                5 -> {
                    when (emotion.first) {
                        "happy" -> binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
                        "sad" -> binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                        "fear" -> binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                        "neutral" -> binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
                        "angry" -> binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
                        "surprise" -> binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                        "disgust" -> binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                    }
                    binding.reactIv5.visibility = View.VISIBLE
                }
                6 -> {
                    when (emotion.first) {
                        "happy" -> binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
                        "sad" -> binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                        "fear" -> binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                        "neutral" -> binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
                        "angry" -> binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
                        "surprise" -> binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                        "disgust" -> binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                    }
                    binding.reactIv6.visibility = View.VISIBLE
                }
                7 -> {
                    when (emotion.first) {
                        "happy" -> binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
                        "sad" -> binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                        "fear" -> binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                        "neutral" -> binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
                        "angry" -> binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
                        "surprise" -> binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                        "disgust" -> binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                    }
                    binding.reactIv7.visibility = View.VISIBLE
                }
            }
            position++
        }
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
        val title = "Request"
        val message =
            "Friend request received from ${sharePref.readString(Constants.FULL_NAME, "")}"
        val recipientToken = messageToken
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

    private fun postNotification(receiverId: String) {
        val time = Helper().localToGMT()
        notificationRef.child(receiverId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val notification = Notification(
                        FirebaseAuth.getInstance().currentUser!!.uid,
                        receiverId,
                        Constants.REQUEST,
                        "Request",
                        "${
                            sharePref.readString(
                                Constants.FULL_NAME,
                                ""
                            )
                        } is sent you friend request", time,
                        sharePref.readString(Constants.PROFILE_PIC, "")!!,
                        "",
                        false,
                        sharePref.readString(Constants.FULL_NAME, "")!!
                    )
                    notificationRef.child(receiverId).push().setValue(notification)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@ProfileDetailActivity,
                        "Error ${error.message}",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })
    }

    private fun sendNotification(notification: PushNotification) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                val response = RetrofitInstance.api.postNotification(notification)
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    binding.followBtn.text = "Requested"
                    binding.followBtn.isEnabled = false
                    Toast.makeText(
                        this@ProfileDetailActivity,
                        "Request send successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d(TAG, "Response: $response")
                } else {
                    Toast.makeText(
                        this@ProfileDetailActivity,
                        "Error:${response.errorBody()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, response.errorBody().toString())
                }
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }
    }

//    private fun fetchToken() {
//        // [START fcm_runtime_enable_auto_init]
//        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
//            if (!task.isSuccessful) {
//                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
//                return@OnCompleteListener
//            }
//
//            // Get new FCM registration token
//            token = task.result
//
//            // Log and toast
//            val msg = getString(R.string.msg_token_fmt, token)
//            Log.d(TAG, msg)
////            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
//        })
//        // [END fcm_runtime_enable_auto_init]
//    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onClick(post: Any, type: String, emotionList: List<Pair<String?, Int>>) {
        TODO("Not yet implemented")
    }
}