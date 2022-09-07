package com.example.ulaugh.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.ulaugh.R
import com.example.ulaugh.adapter.HomeAdapter
import com.example.ulaugh.databinding.FragmentHomeBinding
import com.example.ulaugh.model.HomeRecyclerViewItem
import com.example.ulaugh.model.UserRequest
import com.example.ulaugh.utils.Constants
import com.example.ulaugh.utils.SharePref
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var databaseReference: DatabaseReference? = null
    private var usersDbRef: DatabaseReference? = null
    private var friendsRef: DatabaseReference? = null
    private val storagePath = "All_Image_Uploads/"

    @Inject
    lateinit var sharePref: SharePref
    private var homeList: ArrayList<HomeRecyclerViewItem> = ArrayList()
    private var newsFeedList: ArrayList<HomeRecyclerViewItem.NewsFeed> = ArrayList()
    private var googleAdsList: ArrayList<HomeRecyclerViewItem.GoogleAds> = ArrayList()
    private var usersList: ArrayList<HomeRecyclerViewItem.Friends> = ArrayList()
    private var adapter:HomeAdapter? = null
//    private var friendsList: ArrayList<UserRequest> = ArrayList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.POST_SHARE_REF)
        usersDbRef = FirebaseDatabase.getInstance().getReference(Constants.USERS_REF)
//        friendsRef = FirebaseDatabase.getInstance().getReference(Constants.FRIENDS_REF)

        MobileAds.initialize(activity!!) {}
//        getFriendsList()
//        setAdapter()
        getHomeData()
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
        adapter = HomeAdapter(requireActivity(), homeList)
        binding.recyclerView.adapter = adapter
//        }
    }

    private fun initViews() {
        binding.toolbar1.nameTv.text = sharePref.readString(Constants.FULL_NAME, "")
        binding.toolbar1.statusTv.text = "@${sharePref.readString(Constants.USER_NAME, "")}"
        val imageUrl = sharePref.readString(Constants.PROFILE_PIC, null)
        if (imageUrl != null)
            Glide.with(requireContext())
                .load(imageUrl)
                .centerCrop()
                .fitCenter()
                .thumbnail(0.3f)
                .placeholder(R.drawable.seokangjoon)
                .into(binding.toolbar1.profileIv)
    }

    private fun getHomeData() {
        binding.progressBar.visibility = View.VISIBLE
        homeList = ArrayList()
        usersList = ArrayList()
        var newsFeed: HomeRecyclerViewItem.NewsFeed? = null
        var googleAd: HomeRecyclerViewItem.GoogleAds? = null
        var friends: HomeRecyclerViewItem.Friends? = null
//        databaseReference?.child(FirebaseAuth.getInstance().currentUser!!.uid)!!
//            .addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    binding.txtError.text = ""
//                    binding.progressBar.visibility = View.GONE
//                    for (postSnapshot in snapshot.children) {
//                        newsFeed = postSnapshot.getValue(HomeRecyclerViewItem.NewsFeed::class.java)
////                    val imageUploadInfo: HomeRecyclerViewItem =
////                        postSnapshot.getValue(HomeRecyclerViewItem::class.java)!!
//                        newsFeedList.add(newsFeed!!)
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    binding.progressBar.visibility = View.GONE
//                    binding.txtError.text = error.message
//                }
//
//            })
//        usersDbRef?.child(FirebaseAuth.getInstance().currentUser!!.uid)!!
//            .child(Constants.FRIENDS_REF)
//            .addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    binding.txtError.text = ""
//                    binding.progressBar.visibility = View.GONE
//                    if (snapshot.exists()) {
//                        for (postSnapshot in snapshot.children) {
//                            val friends =
//                                postSnapshot.child(postSnapshot.key!!).child(Constants.FRIENDS_REF)
//                                    .getValue(HomeRecyclerViewItem.Friends::class.java)
//                            usersList.add(friends!!)
//                            if (usersList.size > 10)
//                                break
//                        }
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    binding.progressBar.visibility = View.GONE
//                    binding.txtError.text = error.message
//                }
//            })
        if (usersList.isEmpty()) {
            usersDbRef!!.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.txtError.text = ""
                    binding.progressBar.visibility = View.GONE
                    if (snapshot.child(FirebaseAuth.getInstance().currentUser!!.uid)
                            .child(Constants.FRIENDS_REF).hasChildren()
                    ) {
                        for (postSnapshot in snapshot.children) {
                            val friends =
                                postSnapshot.child(FirebaseAuth.getInstance().currentUser!!.uid)
                                    .child(Constants.FRIENDS_REF).child(postSnapshot.key!!)
                                    .getValue(HomeRecyclerViewItem.Friends::class.java)
                            usersList.add(friends!!)
                            if (usersList.size > 10)
                                break
                        }
                    } else if (snapshot.exists()){
                        for (dataSnap in snapshot.children) {
                            val user:HomeRecyclerViewItem.Friends = dataSnap
                                .getValue(HomeRecyclerViewItem.Friends::class.java)!!
                            usersList.add(user)
                            if (usersList.size > 10)
                                break
                        }
                    }
                    if (newsFeedList.isNotEmpty()) {
                        for (newsItem in 0 until newsFeedList.size) {
                            if (newsItem == 1)
                                homeList.add(
                                    HomeRecyclerViewItem.GoogleAds(
                                        12,
                                        "slajdg",
                                        "lsajdg",
                                        "lsadjg"
                                    )
                                )
                            if (newsItem == 3) {
                                for (user in 0 until usersList.size) {
                                    homeList.add(usersList[user])
                                }
                            }
                        }
                    }else{
                        for (user in 0 until usersList.size) {
                            homeList.add(usersList[user])
                        }
                    }
                    setAdapter()
//                    adapter?.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
        }

//        homeList.add(HomeRecyclerViewItem.NewsFeed(1, "lasdjg"))
//        homeList.add(HomeRecyclerViewItem.GoogleAds(12, "slajdg", "lsajdg", "lsadjg"))
//        homeList.add(HomeRecyclerViewItem.NewsFeed(2, "lasdjg"))
//        homeList.add(HomeRecyclerViewItem.NewsFeed(3, "lasdjg"))
//        homeList.add(HomeRecyclerViewItem.Friends(20, "lsajg", "asldg", 2))
    }

//    private fun getFriendsList(){
//        databaseReference?.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                binding.txtError.text = ""
//                binding.progressBar.visibility = View.GONE
//                for (postSnapshot in snapshot.children) {
//                    newsFeed = postSnapshot.getValue(HomeRecyclerViewItem.NewsFeed::class.java)
////                    val imageUploadInfo: HomeRecyclerViewItem =
////                        postSnapshot.getValue(HomeRecyclerViewItem::class.java)!!
//                    newsFeedList.add(newsFeed!!)
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                binding.progressBar.visibility = View.GONE
//                binding.txtError.text = error.message
//            }
//
//        })
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}