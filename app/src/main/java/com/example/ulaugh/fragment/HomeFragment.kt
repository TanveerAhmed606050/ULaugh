package com.example.ulaugh.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.ulaugh.databinding.FragmentHomeBinding
import com.example.ulaugh.model.PostShareInfo
import com.example.ulaugh.utils.Constants
import com.example.ulaugh.utils.SharePref
import com.google.android.gms.ads.MobileAds
import com.google.firebase.database.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var databaseReference: DatabaseReference? = null
    private val storagePath = "All_Image_Uploads/"

    @Inject
    lateinit var sharePref: SharePref
    private lateinit var homeList: ArrayList<PostShareInfo>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.POST_SHARE_REF)

        MobileAds.initialize(activity!!) {}
//        getHomeData()
        setAdapter()
        clickEvents()
        searchFriend()
        initViews()
    }

    private fun searchFriend() {
        binding.searchTool.searchV.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // on below line we are checking
                // if query exist or not.
//                if (programmingLanguagesList.contains(query)) {
//                    // if query exist within list we
//                    // are filtering our list adapter.
//                    listAdapter.filter.filter(query)
//                } else {
//                    // if query is not present we are displaying
//                    // a toast message as no  data found..
//                    Toast.makeText(activity!!, "No Language found..", Toast.LENGTH_LONG)
//                        .show()
//                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // if query text is change in that case we
                // are filtering our adapter with
                // new text on below line.
//                listAdapter.filter.filter(newText)
                return false
            }
        })
    }

    private fun clickEvents() {
        binding.toolbar1.searchV.setOnClickListener {
            binding.toolbar1.root.visibility = View.INVISIBLE
            binding.searchTool.root.visibility = View.VISIBLE
        }
        binding.searchTool.crossIv.setOnClickListener {
            binding.toolbar1.root.visibility = View.VISIBLE
            binding.searchTool.root.visibility = View.INVISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun setAdapter() {
//        binding.recyclerView.apply {
        binding.recyclerView.setHasFixedSize(true)
//        val adapter = HomeAdapter(requireActivity(), homeList)
//        binding.recyclerView.adapter = adapter
//        }
    }

    private fun initViews() {
        binding.toolbar1.nameTv.text = sharePref.readString(Constants.FULL_NAME, "")
        binding.toolbar1.statusTv.text = "@${sharePref.readString(Constants.USER_NAME, "")}"
        val imageUrl = sharePref.readString(Constants.PROFILE_PIC, null)
        if (imageUrl != null)
            Glide.with(this).load(imageUrl).into(binding.toolbar1.profileIv)
    }

    private fun getHomeData() {
        binding.progressBar.visibility = View.VISIBLE
        homeList = ArrayList()
        databaseReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.txtError.text = ""
                binding.progressBar.visibility = View.GONE
                for (postSnapshot in snapshot.children) {
                    val imageUploadInfo: PostShareInfo =
                        postSnapshot.getValue(PostShareInfo::class.java)!!
                    homeList.add(imageUploadInfo)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBar.visibility = View.GONE
                binding.txtError.text = error.message
            }

        })
//        homeList.add(HomeRecyclerViewItem.NewsFeed(1, "lasdjg"))
//        homeList.add(HomeRecyclerViewItem.GoogleAds(12, "slajdg", "lsajdg", "lsadjg"))
//        homeList.add(HomeRecyclerViewItem.NewsFeed(2, "lasdjg"))
//        homeList.add(HomeRecyclerViewItem.NewsFeed(3, "lasdjg"))
//        homeList.add(HomeRecyclerViewItem.Friends(20, "lsajg", "asldg", 2))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}