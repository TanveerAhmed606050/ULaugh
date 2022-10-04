package com.example.ulaugh.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.ulaugh.model.PostItem
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
    private val postItemsList: MutableList<PostItem> = ArrayList()

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
        if (sharePref.readBoolean(Constants.IS_PRIVATE, false)) {
            binding.lockLogo.visibility = View.VISIBLE
            binding.textView20.visibility = View.VISIBLE
            binding.textView21.visibility = View.VISIBLE
            binding.rv.visibility = View.GONE
        } else {
            binding.lockLogo.visibility = View.GONE
            binding.textView20.visibility = View.GONE
            binding.textView21.visibility = View.GONE
            binding.rv.visibility = View.VISIBLE
        }
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
                        postItemsList.add(postSnap.getValue(PostItem::class.java)!!)
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

    override fun onResume() {
        super.onResume()
        initViews()

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


    override fun onClick(post: Any, type: String, emotionList: List<Emoji>?) {
        TODO("Not yet implemented")
    }
}