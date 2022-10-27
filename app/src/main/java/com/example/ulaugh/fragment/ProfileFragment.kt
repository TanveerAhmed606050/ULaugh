package com.example.ulaugh.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.ulaugh.R
import com.example.ulaugh.adapter.PostsAdapter
import com.example.ulaugh.controller.SettingActivity
import com.example.ulaugh.databinding.FragmentProfileBinding
import com.example.ulaugh.interfaces.AddFriendListener
import com.example.ulaugh.model.*
import com.example.ulaugh.utils.Constants
import com.example.ulaugh.utils.SharePref
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment(), AddFriendListener {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val postItemsList: MutableList<HomeRecyclerViewItem.SharePostData> = ArrayList()

//    private val authViewModel by activityViewModels<AuthViewModel>()

    @Inject
    lateinit var sharePref: SharePref
    private lateinit var profileDataRef: DatabaseReference
    private lateinit var followerRef: DatabaseReference
    lateinit var postsAdapter: PostsAdapter
    var userName = ""
    var fullName = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        profileDataRef = FirebaseDatabase.getInstance().getReference(Constants.POST_SHARE_REF)
        followerRef = FirebaseDatabase.getInstance().getReference(Constants.USERS_REF)

        setAdapter()
        clickListener()
        CoroutineScope(Dispatchers.Main).launch {
            binding.progressBar.visibility = View.VISIBLE
            getProfileData()
            getFollowers()
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun initViews() {
        val url = sharePref.readString(Constants.PROFILE_PIC, null)
        userName = sharePref.readString(Constants.USER_NAME, "").toString()
        fullName = sharePref.readString(Constants.FULL_NAME, "").toString()

        if (url != null) {
            Glide.with(requireContext())
                .load(url)
                .centerCrop()
                .fitCenter()
                .thumbnail()
                .placeholder(R.drawable.user_logo)
                .into(binding.profileIv)

            Glide.with(requireContext())
                .load(url)
                .centerCrop()
                .fitCenter()
                .thumbnail()
                .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 4)))
                .placeholder(R.drawable.user_logo)
                .into(binding.coverIv)
        }
        binding.nameTv.text = fullName
        binding.idTv.text = "@$userName"
//        if (sharePref.readBoolean(Constants.IS_PRIVATE, false)) {
//            binding.lockLogo.visibility = View.VISIBLE
//            binding.textView20.visibility = View.VISIBLE
//            binding.textView21.visibility = View.VISIBLE
//            binding.rv.visibility = View.GONE
//        } else {
//            binding.lockLogo.visibility = View.GONE
//            binding.textView20.visibility = View.GONE
//            binding.textView21.visibility = View.GONE
//            binding.rv.visibility = View.VISIBLE
//        }
    }

    private fun setAdapter() {
        binding.rv.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        postsAdapter = PostsAdapter(requireActivity(), postItemsList, this)
        binding.rv.adapter = postsAdapter
    }

    private fun getProfileData() {
        profileDataRef.child(FirebaseAuth.getInstance().currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    postItemsList.clear()
                    for (postSnap in snapshot.children) {
                        val keyValue = postSnap.key.toString()
                        val userReaction = ""
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
                            post._profile_pic,
                            userReaction,
                        )
                        Log.d(Constants.TAG, "onDataChange: ${userReaction}\n")
                        postItemsList.add(postItem)
                        if (post._profile_pic == "true"){
                            val emotionsList = countReactions(reactionsList)
                            setEmotions(emotionsList, requireContext())
                        }
                    }
                    binding.postTv.text = "${postItemsList.size}"
                    postsAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        requireContext(),
                        "Canceled: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            })
    }

    private fun getFollowers(): Int {
        var followerCount = 0
        followerRef.child(FirebaseAuth.getInstance().currentUser!!.uid).child(Constants.FRIENDS_REF)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (friendSnap in snapshot.children) {
                        val friend = friendSnap.getValue(Friend::class.java)
                        if (friend!!._follow!!)
                            followerCount++
                    }
                    binding.followerTv.text = "$followerCount"
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        return followerCount
    }

    private fun clickListener() {
        binding.menuIv.setOnClickListener {
            startActivity(Intent(context, SettingActivity::class.java))
        }
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

    override fun onResume() {
        super.onResume()
        initViews()

    }

    override fun onDestroy() {
        super.onDestroy()
//        _binding = null
    }


    override fun onClick(post: Any, type: String, emotionList: List<Pair<String?, Int>>) {
        TODO("Not yet implemented")
    }
}