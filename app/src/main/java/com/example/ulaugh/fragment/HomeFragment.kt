package com.example.ulaugh.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.ulaugh.R
import com.example.ulaugh.adapter.HomeAdapter
import com.example.ulaugh.controller.CameraActivity
import com.example.ulaugh.controller.ProfileDetailActivity
import com.example.ulaugh.controller.ReactDetailActivity
import com.example.ulaugh.databinding.FragmentHomeBinding
import com.example.ulaugh.interfaces.OnClickListener
import com.example.ulaugh.interfaces.PostClickListener
import com.example.ulaugh.model.*
import com.example.ulaugh.utils.Constants
import com.example.ulaugh.utils.Constants.TAG
import com.example.ulaugh.utils.Constants.USER_NAME
import com.example.ulaugh.utils.SharePref
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class HomeFragment : Fragment(), OnClickListener, PostClickListener {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var postShareRef: DatabaseReference? = null
    private var allUsersRef: DatabaseReference? = null
    private var friendsRef: DatabaseReference? = null

    @Inject
    lateinit var sharePref: SharePref
    private var homeList: ArrayList<HomeRecyclerViewItem> = ArrayList()
    private var newsFeedList: ArrayList<HomeRecyclerViewItem.SharePostData> = ArrayList()

    //    private var postsList: ArrayList<HomeRecyclerViewItem.SharePostData> = ArrayList()
//    private var googleAdsList: ArrayList<HomeRecyclerViewItem.GoogleAds> = ArrayList()
    private val friendsList: ArrayList<String> = ArrayList()
    private var suggestFriendsList: ArrayList<SuggestFriends> = ArrayList()
    private var adapter: HomeAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postShareRef = FirebaseDatabase.getInstance().getReference(Constants.POST_SHARE_REF)
        allUsersRef = FirebaseDatabase.getInstance().getReference(Constants.USERS_REF)
        friendsRef = FirebaseDatabase.getInstance().getReference(Constants.FRIENDS_REF)

        MobileAds.initialize(activity!!) {}
        setAdapter()
        CoroutineScope(Dispatchers.Main).launch {
            binding.progressBar.visibility = View.VISIBLE
            getFriends()
//            delay(600)
//            getHomeData()
            delay(3000)
            getSuggestedFriends()
            binding.progressBar.visibility = View.GONE
        }
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

//                listAdapter.filter.filter(newText)
                return false
            }
        })
    }

    private fun clickEvents() {
        binding.toolbar1.searchV1.setOnClickListener {
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
        adapter = HomeAdapter(requireActivity(), homeList, this, this)
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

    private fun getFriends() {
        allUsersRef!!.child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(Constants.FRIENDS_REF).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
//                    Toast.makeText(
//                        requireContext(),
//                        "${snapshot.children.count()}",
//                        Toast.LENGTH_SHORT
//                    ).show()
                    if (snapshot.hasChildren()) {
                        for (friendsSnap in snapshot.children) {
                            friendsList.add(friendsSnap.value.toString())
                        }
                        getHomeData()
                    } else {
                        Toast.makeText(requireContext(), "No friends", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error:${error.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun getHomeData() {
        val reactionsList: ArrayList<Reactions> = ArrayList()
        reactionsList.clear()
        for (friendId in friendsList) {
            postShareRef!!.child(friendId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (postSnap in snapshot.children) {
                        val keyValue = postSnap.key.toString()
//                        val totalReactions = postSnap.child(Constants.FRIENDS_REF).childrenCount
                        var userReaction = ""

                        reactionsList.clear()
//                        val reactions = Reactions()
                        for (reactionItem in postSnap.child(Constants.REACTION).children) {
                            if (reactionItem
                                    .child("user_id")
                                    .equals(FirebaseAuth.getInstance().currentUser!!.uid)
                            ) //current user reaction
                                userReaction = postSnap.child(Constants.REACTION)
                                    .child(Constants.REACTION_TYPE).value.toString()
                            val reactions = reactionItem.getValue(Reactions::class.java)
//                            reactions.reaction_type = reactionsType
//                            reactions!!.user_id = reactionItem.key.toString()
                            reactionsList.add(reactions!!)
                        }
//                        val reactionDetail = ReactionDetail(totalReactions, reacted)
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
                            post.tagsList, reactionsList, userReaction
                        )
                        Log.d(TAG, "onDataChange: ${userReaction}\n")
                        newsFeedList.add(postItem)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error:${error.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
        }
    }

    private fun getSuggestedFriends() {
//        binding.progressBar.visibility = View.VISIBLE
        homeList = ArrayList()
        suggestFriendsList = ArrayList()

        allUsersRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                suggestFriendsList.clear()
                homeList.clear()
//                binding.progressBar.visibility = View.GONE
                for (userSnap in snapshot.children) {
                    val userData =
                        userSnap.getValue(SuggestFriends::class.java)
                    if (userData?.firebase_id != FirebaseAuth.getInstance().currentUser!!.uid) {//already have a friend or not
                        allUsersRef!!.child(FirebaseAuth.getInstance().currentUser!!.uid)
                            .child(Constants.FRIENDS_REF)
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    var isFound = false
                                    for (userItemSnap in snapshot.children) {
                                        val friendId = userItemSnap.value.toString()
                                        if (userData!!.firebase_id == friendId) {
                                            isFound = true
                                            break
                                        }
                                    }
                                    if (!isFound) {
                                        suggestFriendsList.add(userData!!)
                                        binding.recyclerView.adapter!!.notifyDataSetChanged()
                                    }
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
                    if (suggestFriendsList.size > 10)
                        break
                }
                populateData()

//                if (newsFeedList.isNotEmpty()) {
//                    for (newsItem in 0 until newsFeedList.size) {
//                        if (newsItem == 1)
//                            homeList.add(
//                                HomeRecyclerViewItem.GoogleAds(
//                                    12,
//                                    "slajdg",
//                                    "lsajdg",
//                                    "lsadjg"
//                                )
//                            )
//                        if (newsItem == 2) {
//                            val suggestList =
//                                HomeRecyclerViewItem.SuggestList(suggestFriendsList)
//                            homeList.add(suggestList)
//                        }
//                    }
//                } else {
//                    val suggestList = HomeRecyclerViewItem.SuggestList(suggestFriendsList)
//                    homeList.add(suggestList)
//                }
//                setAdapter()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Canceled: ${error.message}", Toast.LENGTH_SHORT)
                    .show()
            }

        })

//        friendsRef!!.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                binding.txtError.text = ""
//                binding.progressBar.visibility = View.GONE
//                if (snapshot.child(FirebaseAuth.getInstance().currentUser!!.uid).hasChildren()
//                ) {
//                    for (postSnapshot in snapshot.children) {
//                        val friendsId =
//                            postSnapshot.child(FirebaseAuth.getInstance().currentUser!!.uid)
//                                .child(postSnapshot.key!!)
//                                .child(Constants.FIREBASE_ID).value.toString()
//                        friendsRef!!.child(friendsId)
//                            .addValueEventListener(object : ValueEventListener {
//                                override fun onDataChange(snapshot: DataSnapshot) {
//                                    for (userSnap in snapshot.children) {
//                                        val friends = userSnap.child(userSnap.key!!).getValue(SuggestFriends::class.java)
//                                        suggestFriendsList.add(friends!!)
//                                        if (suggestFriendsList.size > 10)
//                                            break
//                                    }
//                                }
//
//                                override fun onCancelled(error: DatabaseError) {
//                                }
//
//                            })
//
//                    }
//                } else if (snapshot.exists()) {
//                    allUsersRef!!.addValueEventListener(object : ValueEventListener {
//                        override fun onDataChange(snapshot: DataSnapshot) {
//                            for (dataSnap in snapshot.children) {
//                                val user: SuggestFriends = dataSnap
//                                    .getValue(SuggestFriends::class.java)!!
//                                if (user.firebase_id != FirebaseAuth.getInstance().currentUser!!.uid)
//                                    suggestFriendsList.add(user)
//                                if (suggestFriendsList.size > 10)
//                                    break
//                            }
//                        }
//
//                        override fun onCancelled(error: DatabaseError) {
//                        }
//                    })
//                }
//                if (newsFeedList.isNotEmpty()) {
//                    for (newsItem in 0 until newsFeedList.size) {
//                        if (newsItem == 1)
//                            homeList.add(
//                                HomeRecyclerViewItem.GoogleAds(
//                                    12,
//                                    "slajdg",
//                                    "lsajdg",
//                                    "lsadjg"
//                                )
//                            )
//                        if (newsItem == 2) {
//                            val suggestList =
//                                HomeRecyclerViewItem.SuggestList(suggestFriendsList)
//                            homeList.add(suggestList)
//                        }
//                    }
//                } else {
//                    val suggestList = HomeRecyclerViewItem.SuggestList(suggestFriendsList)
//                    homeList.add(suggestList)
//                }
//                setAdapter()
////                    adapter?.notifyDataSetChanged()
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//            }
//
//        })

//        homeList.add(HomeRecyclerViewItem.NewsFeed(1, "lasdjg"))
//        homeList.add(HomeRecyclerViewItem.GoogleAds(12, "slajdg", "lsajdg", "lsadjg"))
//        homeList.add(HomeRecyclerViewItem.NewsFeed(2, "lasdjg"))
//        homeList.add(HomeRecyclerViewItem.NewsFeed(3, "lasdjg"))
//        homeList.add(HomeRecyclerViewItem.Friends(20, "lsajg", "asldg", 2))
    }

    private fun populateData() {
        if (newsFeedList.isNotEmpty()) {
            for (newsItem in 0 until newsFeedList.size) {
                homeList.add(newsFeedList[newsItem])
//                if (newsItem == 1)
//                    homeList.add(
//                        HomeRecyclerViewItem.GoogleAds(
//                            12,
//                            "slajdg",
//                            "lsajdg",
//                            "lsadjg"
//                        )
//                    )
//                if (newsItem == 2) {
//                    val suggestList =
//                        HomeRecyclerViewItem.SuggestList(suggestFriendsList)
//                    homeList.add(suggestList)
//                }
            }
        } else {

        }
        val suggestList = HomeRecyclerViewItem.SuggestList(suggestFriendsList)
        homeList.add(suggestList)
        setAdapter()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(suggestFriends: SuggestFriends) {
        binding.progressBar.visibility = View.VISIBLE
        allUsersRef!!
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        allUsersRef!!.child(FirebaseAuth.getInstance().currentUser!!.uid)
                            .child(Constants.FRIENDS_REF)
                            .runTransaction(object : Transaction.Handler {
                                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                                    var lastKey = "-1"
                                    for (child in mutableData.children) {
                                        lastKey = child.key!!
                                    }
                                    val nextKey = lastKey.toInt() + 1
                                    mutableData.child("" + nextKey).value =
                                        suggestFriends.firebase_id

                                    // Set value and report transaction success
                                    return Transaction.success(mutableData)
                                }

                                override fun onComplete(
                                    databaseError: DatabaseError?, b: Boolean,
                                    dataSnapshot: DataSnapshot?
                                ) {
                                    binding.progressBar.visibility = View.GONE
                                    Toast.makeText(
                                        requireContext(),
                                        "Added Friend successfully",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
//                                    suggestFriendsList.remove(suggestFriends)
//                                    val suggestList =
//                                        HomeRecyclerViewItem.SuggestList(suggestFriendsList)
//                                    homeList[homeList.size-1] = suggestList
//                                    binding.recyclerView.adapter!!.notifyDataSetChanged()
                                }
                            })
                    } else {
                        allUsersRef!!.child(FirebaseAuth.getInstance().currentUser!!.uid)
                            .child(Constants.FRIENDS_REF)
                            .runTransaction(object : Transaction.Handler {
                                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                                    var lastKey = "-1"
                                    for (child in mutableData.children) {
                                        lastKey = child.key!!
                                    }
                                    val nextKey = lastKey.toInt() + 1
                                    mutableData.child("" + nextKey).value =
                                        suggestFriends.firebase_id
                                    // Set value and report transaction success
                                    return Transaction.success(mutableData)
                                }

                                override fun onComplete(
                                    databaseError: DatabaseError?, b: Boolean,
                                    dataSnapshot: DataSnapshot?
                                ) {
                                    binding.progressBar.visibility = View.GONE
                                    Toast.makeText(
                                        requireContext(),
                                        "Add friends Successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
//                                    suggestFriendsList.remove(suggestFriends)
//                                    val suggestList =
//                                        HomeRecyclerViewItem.SuggestList(suggestFriendsList)
//                                    homeList[homeList.size-1] = suggestList
//                                    homeList.add(suggestList)
//                                    binding.recyclerView.adapter!!.notifyDataSetChanged()
                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onClick(post: Any, type: String) {
        when (type) {
            Constants.POST -> {
                val intent = Intent(requireContext(), ReactDetailActivity::class.java)
                intent.putExtra(Constants.POST, Gson().toJson(post))
                requireContext().startActivity(intent)
            }
            Constants.REACTION -> {
                val intent = Intent(requireContext(), CameraActivity::class.java)
                intent.putExtra(Constants.POST, Gson().toJson(post))
                requireContext().startActivity(intent)
            }
            Constants.PROFILE -> {
                val postData = post as HomeRecyclerViewItem.SharePostData
                val intent = Intent(requireContext(), ProfileDetailActivity::class.java)
                intent.putExtra(Constants.FIREBASE_ID, postData.firebase_id)
                requireContext().startActivity(intent)
//                val sharePostData = post as HomeRecyclerViewItem.SharePostData
//                val transaction = parentFragmentManager.beginTransaction()
//                transaction.replace(R.id.parent, ProfileFragment(sharePostData.firebase_id))
//                transaction.disallowAddToBackStack()
//                transaction.commit()

            }
        }
    }


}