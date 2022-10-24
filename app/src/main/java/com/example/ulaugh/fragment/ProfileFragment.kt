package com.example.ulaugh.fragment

import android.content.Context
import android.content.Intent
import android.os.Build
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
import com.example.ulaugh.interfaces.PostClickListener
import com.example.ulaugh.model.Emoji
import com.example.ulaugh.model.HomeRecyclerViewItem
import com.example.ulaugh.model.PostItem
import com.example.ulaugh.model.Reactions
import com.example.ulaugh.utils.Constants
import com.example.ulaugh.utils.SharePref
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment(), PostClickListener {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val postItemsList: MutableList<HomeRecyclerViewItem.SharePostData> = ArrayList()

//    private val authViewModel by activityViewModels<AuthViewModel>()

    @Inject
    lateinit var sharePref: SharePref
    private lateinit var databaseReference: DatabaseReference
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

        databaseReference =
            FirebaseDatabase.getInstance().getReference(Constants.POST_SHARE_REF)
        setAdapter()
        clickListener()
        CoroutineScope(Dispatchers.Main).launch {
            binding.progressBar.visibility = View.VISIBLE
            getProfileData()
            binding.progressBar.visibility = View.GONE
        }
//        postItems.add(PostItem(R.drawable.seokangjoon))
//        postItems.add(PostItem(R.drawable.parkseojoon))
//        postItems.add(PostItem(R.drawable.yooseungho))
//        postItems.add(PostItem(R.drawable.seokangjoon))
//        postItems.add(PostItem(R.drawable.parkseojoon))
//        postItems.add(PostItem(R.drawable.yooseungho))
//        postItems.add(PostItem(R.drawable.seokangjoon))
//        postItems.add(PostItem(R.drawable.parkseojoon))
//        postItems.add(PostItem(R.drawable.yooseungho))
//        postItems.add(PostItem(R.drawable.seokangjoon))

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
                .placeholder(R.drawable.seokangjoon)
                .into(binding.profileIv)

            Glide.with(requireContext())
                .load(url)
                .centerCrop()
                .fitCenter()
                .thumbnail()
                .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 4)))
                .placeholder(R.drawable.seokangjoon)
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
        databaseReference.child(FirebaseAuth.getInstance().currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    postItemsList.clear()
                    for (postSnap in snapshot.children) {
                        val keyValue = postSnap.key.toString()
//                        val totalReactions = postSnap.child(Constants.FRIENDS_REF).childrenCount
                        var userReaction = ""
                        val reactionsList: MutableList<Reactions> = ArrayList()

                        for (reactionItem in postSnap.child(Constants.REACTION).children) {
                            val reactions = reactionItem.getValue(Reactions::class.java)!!
//                            if (reactions.user_id == FirebaseAuth.getInstance().currentUser!!.uid)
//                                userReaction = reactions.reaction_type!!
//                                else
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
                        //                        postItemsList.add(postSnap.getValue(HomeRecyclerViewItem.SharePostData::class.java)!!)
//                        postItemsList.add(
//                            PostItem(
//                                dataSnapshot.key.toString(),
//                                dataSnapshot.child(Constants.DATE_TIME).value.toString(),
//                                dataSnapshot.child(Constants.DESCRIPTION).value.toString(),
//                                dataSnapshot.child(Constants.IMAGE_URL).value.toString(),
//                                dataSnapshot.child(Constants.TAGS_LIST).value.toString()
//                            )
//                        )
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

    private fun clickListener() {
        binding.menuIv.setOnClickListener {
            startActivity(Intent(context, SettingActivity::class.java))
        }
    }

    private fun setEmotions(emotionsList: List<Emoji>, context: Context) {
        var position = 1 //set half emotions
        for (emotion in emotionsList) {
            when (position) {
                1 -> {
                    when (emotion.name) {
                        "happy" -> {
                            binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
                            binding.reactIv1.visibility = View.VISIBLE
                        }
                        "sad" -> {
                            binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                            binding.reactIv1.visibility = View.VISIBLE
                        }
                        "fear" -> {
                            binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                            binding.reactIv1.visibility = View.VISIBLE
                        }
                        "neutral" -> {
                            binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
                            binding.reactIv1.visibility = View.VISIBLE
                        }
                        "angry" -> {
                            binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
                            binding.reactIv1.visibility = View.VISIBLE
                        }
                        "surprise" -> {
                            binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                            binding.reactIv1.visibility = View.VISIBLE
                        }
                        "disgust" -> {
                            binding.reactIv1.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                            binding.reactIv1.visibility = View.VISIBLE
                        }
                    }
                }
                2 -> {
                    when (emotion.name) {
                        "happy" -> {
                            binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
                            binding.reactIv2.visibility = View.VISIBLE
                        }
                        "sad" -> {
                            binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                            binding.reactIv2.visibility = View.VISIBLE
                        }
                        "fear" -> {
                            binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                            binding.reactIv2.visibility = View.VISIBLE
                        }
                        "neutral" -> {
                            binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
                            binding.reactIv2.visibility = View.VISIBLE
                        }
                        "angry" -> {
                            binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
                            binding.reactIv2.visibility = View.VISIBLE
                        }
                        "surprise" -> {
                            binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                            binding.reactIv2.visibility = View.VISIBLE
                        }
                        "disgust" -> {
                            binding.reactIv2.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                            binding.reactIv2.visibility = View.VISIBLE
                        }
                    }
                }
                3 -> {
                    when (emotion.name) {
                        "happy" -> {
                            binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
                            binding.reactIv3.visibility = View.VISIBLE
                        }
                        "sad" -> {
                            binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                            binding.reactIv3.visibility = View.VISIBLE
                        }
                        "fear" -> {
                            binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                            binding.reactIv3.visibility = View.VISIBLE
                        }
                        "neutral" -> {
                            binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
                            binding.reactIv3.visibility = View.VISIBLE
                        }
                        "angry" -> {
                            binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
                            binding.reactIv3.visibility = View.VISIBLE
                        }
                        "surprise" -> {
                            binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                            binding.reactIv3.visibility = View.VISIBLE
                        }
                        "disgust" -> {
                            binding.reactIv3.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                            binding.reactIv3.visibility = View.VISIBLE
                        }
                    }
                }
                4 -> {
                    when (emotion.name) {
                        "happy" -> {
                            binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
                            binding.reactIv4.visibility = View.VISIBLE
                        }
                        "sad" -> {
                            binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                            binding.reactIv4.visibility = View.VISIBLE
                        }
                        "fear" -> {
                            binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                            binding.reactIv4.visibility = View.VISIBLE
                        }
                        "neutral" -> {
                            binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
                            binding.reactIv4.visibility = View.VISIBLE
                        }
                        "angry" -> {
                            binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
                            binding.reactIv4.visibility = View.VISIBLE
                        }
                        "surprise" -> {
                            binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                            binding.reactIv4.visibility = View.VISIBLE
                        }
                        "disgust" -> {
                            binding.reactIv4.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                            binding.reactIv4.visibility = View.VISIBLE
                        }
                    }
                }
                5 -> {
                    when (emotion.name) {
                        "happy" -> {
                            binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
                            binding.reactIv5.visibility = View.VISIBLE
                        }
                        "sad" -> {
                            binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                            binding.reactIv5.visibility = View.VISIBLE
                        }
                        "fear" -> {
                            binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                            binding.reactIv5.visibility = View.VISIBLE
                        }
                        "neutral" -> {
                            binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
                            binding.reactIv5.visibility = View.VISIBLE
                        }
                        "angry" -> {
                            binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
                            binding.reactIv5.visibility = View.VISIBLE
                        }
                        "surprise" -> {
                            binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                            binding.reactIv5.visibility = View.VISIBLE
                        }
                        "disgust" -> {
                            binding.reactIv5.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                            binding.reactIv5.visibility = View.VISIBLE
                        }
                    }
                }
                6 -> {
                    when (emotion.name) {
                        "happy" -> {
                            binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
                            binding.reactIv6.visibility = View.VISIBLE
                        }
                        "sad" -> {
                            binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                            binding.reactIv6.visibility = View.VISIBLE
                        }
                        "fear" -> {
                            binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                            binding.reactIv6.visibility = View.VISIBLE
                        }
                        "neutral" -> {
                            binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
                            binding.reactIv6.visibility = View.VISIBLE
                        }
                        "angry" -> {
                            binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
                            binding.reactIv6.visibility = View.VISIBLE
                        }
                        "surprise" -> {
                            binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                            binding.reactIv6.visibility = View.VISIBLE
                        }
                        "disgust" -> {
                            binding.reactIv6.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                            binding.reactIv6.visibility = View.VISIBLE
                        }
                    }
                }
                7 -> {
                    when (emotion.name) {
                        "happy" -> {
                            binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.haha_ic))
                            binding.reactIv7.visibility = View.VISIBLE
                        }
                        "sad" -> {
                            binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                            binding.reactIv7.visibility = View.VISIBLE
                        }
                        "fear" -> {
                            binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                            binding.reactIv7.visibility = View.VISIBLE
                        }
                        "neutral" -> {
                            binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.neutral_ic))
                            binding.reactIv7.visibility = View.VISIBLE
                        }
                        "angry" -> {
                            binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.anger_emotion))
                            binding.reactIv7.visibility = View.VISIBLE
                        }
                        "surprise" -> {
                            binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.fear_ic))
                            binding.reactIv7.visibility = View.VISIBLE
                        }
                        "disgust" -> {
                            binding.reactIv7.setImageDrawable(context.getDrawable(R.drawable.sad_ic))
                            binding.reactIv7.visibility = View.VISIBLE
                        }
                    }
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