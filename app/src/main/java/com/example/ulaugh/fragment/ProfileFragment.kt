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
import com.example.ulaugh.model.PostItem
import com.example.ulaugh.utils.Constants
import com.example.ulaugh.utils.SharePref
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
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val postItemsList: MutableList<PostItem> = mutableListOf()

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


        initViews()
        createBlurImage()
        clickListener()
        CoroutineScope(Dispatchers.IO).launch {
            getProfileData()
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
        databaseReference =
            FirebaseDatabase.getInstance().reference.child(Constants.POST_SHARE_REF)

        val url = sharePref.readString(Constants.PROFILE_PIC, null)
        userName = sharePref.readString(Constants.USER_NAME, "").toString()
        fullName = sharePref.readString(Constants.FULL_NAME, "").toString()

        if (url != null)
            Glide.with(this).load(url).into(binding.profileIv)
        binding.nameTv.text = fullName
        binding.idTv.text = "@$userName"
        binding.rv.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        postsAdapter = PostsAdapter(requireActivity(), postItemsList)
        binding.rv.adapter = postsAdapter
    }

    private fun getProfileData() {
        binding.progressBar.visibility = View.VISIBLE
        databaseReference.child(Firebase.auth.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (dataSnapshot in snapshot.children) {
                        postItemsList.add(
                            PostItem(
                                dataSnapshot.key.toString(),
                                dataSnapshot.child(Constants.DATE_TIME).value.toString(),
                                dataSnapshot.child(Constants.DESCRIPTION).value.toString(),
                                dataSnapshot.child(Constants.IMAGE_URL).value.toString(),
                                dataSnapshot.child(Constants.TAGS_LIST).value.toString()
                            )
                        )
                    }
                    binding.progressBar.visibility = View.GONE
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

    private fun createBlurImage() {
        //Get seekBar progress
        Glide.with(this).load(R.drawable.seokangjoon)
            .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 4)))
            .into(binding.coverIv)
    }

    private fun clickListener() {
        binding.menuIv.setOnClickListener {
            startActivity(Intent(context, SettingActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}