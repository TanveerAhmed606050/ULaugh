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
import com.example.ulaugh.adapter.SearchFilterAdapter
import com.example.ulaugh.controller.CameraActivity
import com.example.ulaugh.controller.ProfileDetailActivity
import com.example.ulaugh.controller.ReactDetailActivity
import com.example.ulaugh.databinding.FragmentHomeBinding
import com.example.ulaugh.interfaces.FollowFriendListener
import com.example.ulaugh.interfaces.OnClickListener
import com.example.ulaugh.interfaces.AddFriendListener
import com.example.ulaugh.model.*
import com.example.ulaugh.utils.Constants
import com.example.ulaugh.utils.Constants.TAG
import com.example.ulaugh.utils.Helper
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
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


@AndroidEntryPoint
class HomeFragment : Fragment(), OnClickListener, AddFriendListener, FollowFriendListener {
    //    private var _binding: FragmentHomeBinding? = null
    private var binding: FragmentHomeBinding? = null
    private var postShareRef: DatabaseReference? = null
    private var allUsersRef: DatabaseReference? = null
    private var notificationRef: DatabaseReference? = null

    @Inject
    lateinit var sharePref: SharePref
    private var homeList: ArrayList<HomeRecyclerViewItem> = ArrayList()
    private var newsFeedList: ArrayList<HomeRecyclerViewItem.SharePostData> = ArrayList()

    //    private var postsList: ArrayList<HomeRecyclerViewItem.SharePostData> = ArrayList()
//    private var googleAdsList: ArrayList<HomeRecyclerViewItem.GoogleAds> = ArrayList()
    private val friendsList: ArrayList<Friend> = ArrayList()

    //    private val searchList: ArrayList<Friend> = ArrayList()
    private var suggestFriendsList: ArrayList<SuggestFriends> = ArrayList()
    private var adapter: HomeAdapter? = null
    private var suggestList: HomeRecyclerViewItem.SuggestList? = null
    var lastFriend = ""
    lateinit var searchFilterAdapter: SearchFilterAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        MobileAds.initialize(activity!!) {}
//        setAdapter()
        clickEvents()
        searchAdapterFun()
        searchFriend()
        initViews()
    }

    private fun clickEvents() {
        binding!!.toolbar1.searchV1.setOnClickListener {
            binding!!.toolbar1.root.visibility = View.INVISIBLE
            binding!!.searchTool.root.visibility = View.VISIBLE
            searchAdapterFun()
            binding!!.textView31.visibility = View.GONE
            binding!!.recyclerView.visibility = View.GONE
        }
        binding!!.searchTool.crossIv.setOnClickListener {
            binding!!.toolbar1.root.visibility = View.VISIBLE
            binding!!.searchTool.root.visibility = View.INVISIBLE
            binding!!.recyclerView.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    private fun setAdapter() {
//        binding!!.recyclerView.apply {
            binding!!.recyclerView.setHasFixedSize(true)
            adapter = HomeAdapter(requireContext(), homeList, this, this, this)
            binding!!.recyclerView.adapter = adapter
//        }
    }

    private fun initViews() {
        postShareRef = FirebaseDatabase.getInstance().getReference(Constants.POST_SHARE_REF)
        allUsersRef = FirebaseDatabase.getInstance().getReference(Constants.USERS_REF)
        notificationRef = FirebaseDatabase.getInstance().getReference(Constants.NOTIFICATION)
        binding!!.toolbar1.nameTv.text = sharePref.readString(Constants.FULL_NAME, "")
        binding!!.toolbar1.statusTv.text = "@${sharePref.readString(Constants.USER_NAME, "")}"
        val imageUrl = sharePref.readString(Constants.PROFILE_PIC, null)
        if (imageUrl != null)
            Glide.with(requireContext())
                .load(imageUrl)
                .centerCrop()
                .fitCenter()
//                .thumbnail(0.3f)
                .placeholder(R.drawable.user_logo)
                .into(binding!!.toolbar1.profileIv)
    }

    private fun lastFriendId() {
        allUsersRef!!.child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(Constants.FRIENDS_REF).limitToLast(1)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        val friend = snapshot.getValue(Friend::class.java)
                        //Add your food to the list
                        lastFriend = friend!!.firebase_id.toString()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    println("The read failed: " + databaseError.code)
                }
            })
    }

    private fun getFriends() {
        allUsersRef!!.child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(Constants.FRIENDS_REF).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChildren()) {
                        for (friendsSnap in snapshot.children) {
                            val friend = friendsSnap.getValue(Friend::class.java)
                            friendsList.add(friendsSnap.getValue(Friend::class.java)!!)
                            if (friend!!.firebase_id == lastFriend || lastFriend.isEmpty())
                                getHomeData()
//                            Log.d(TAG, "FriendList: ${friendsList}")
                        }
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
//        reactionsList.clear()
        for (friendDetail in friendsList) {
            postShareRef!!.child(friendDetail.firebase_id!!)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (postSnap in snapshot.children) {
                            val keyValue = postSnap.key.toString()
//                        val totalReactions = postSnap.child(Constants.FRIENDS_REF).childrenCount
                            var userReaction = ""
                            val reactionsList: MutableList<Reactions> = ArrayList()

                            for (reactionItem in postSnap.child(Constants.REACTION).children) {
                                val reactions = reactionItem.getValue(Reactions::class.java)!!
                                if (reactions.user_id == FirebaseAuth.getInstance().currentUser!!.uid)
                                    userReaction = reactions.reaction_type!!
//                            reactions.reaction_type = reactionsType
//                            reactions!!.user_id = reactionItem.key.toString()
                                reactionsList.add(reactions)
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
                                post.tagsList,
                                post.profile_image,
                                reactionsList,
                                post._profile_pic,
                                userReaction, friendDetail._follow!!
                            )
                            Log.d(TAG, "onDataChange: ${userReaction}\n")
                            newsFeedList.add(postItem)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            requireContext(),
                            "Error:${error.message}",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                })
        }
    }

    private fun getSuggestedFriends() {
//        binding.progressBar.visibility = View.VISIBLE
        homeList = ArrayList()
        suggestFriendsList = ArrayList()
        homeList.clear()

        allUsersRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                suggestFriendsList.clear()
                if (suggestList != null)
                    suggestList!!.friends.clear()

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
                                        val friendId = userItemSnap.getValue(Friend::class.java)
                                        if (userData!!.firebase_id == friendId!!.firebase_id) {
                                            isFound = true
                                            break
                                        }
                                    }
                                    if (!isFound) {
                                        suggestFriendsList.add(userData!!)
                                        binding!!.recyclerView.adapter!!.notifyDataSetChanged()
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

    }

    private fun populateData() {
//        var suggestList:HomeRecyclerViewItem.SuggestList
        if (newsFeedList.isNotEmpty()) {
            newsFeedList = getRandomItemFromList()
            for (newsItem in 0 until newsFeedList.size) {
                homeList.add(newsFeedList[newsItem])
                if (newsFeedList.size < 2) {
                    homeList.add(
                        HomeRecyclerViewItem.GoogleAds(
                            12,
                            "slajdg",
                            "lsajdg",
                            "lsadjg"
                        )
                    )
                    suggestList =
                        HomeRecyclerViewItem.SuggestList(suggestFriendsList)
                    homeList.add(suggestList!!)
                } else {
                    when (newsItem) {
                        1 -> {
                            homeList.add(
                                HomeRecyclerViewItem.GoogleAds(
                                    12,
                                    "slajdg",
                                    "lsajdg",
                                    "lsadjg"
                                )
                            )
//                            suggestList =
//                                HomeRecyclerViewItem.SuggestList(suggestFriendsList)
//                            homeList.add(suggestList!!)
                        }
                        2 -> {
                            suggestList =
                                HomeRecyclerViewItem.SuggestList(suggestFriendsList)
                            homeList.add(suggestList!!)
                        }
                    }
                }
            }
        } else {
            suggestList = HomeRecyclerViewItem.SuggestList(suggestFriendsList)
            homeList.add(suggestList!!)
        }
//        binding!!.recyclerView.adapter!!.notifyDataSetChanged()
        setAdapter()
    }

    private fun getRandomItemFromList(): ArrayList<HomeRecyclerViewItem.SharePostData> {
        val newHomeArray = ArrayList<HomeRecyclerViewItem.SharePostData>()
        for (i in 0 until newsFeedList.size) {
            val random = (0 until newsFeedList.size).random()
            newHomeArray.add(newsFeedList[random])
            newsFeedList.removeAt(random)
        }
        return newHomeArray
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        _binding = null
    }

    override fun onClick(suggestFriends: SuggestFriends) {
        binding!!.progressBar.visibility = View.VISIBLE
        allUsersRef!!
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
//                    val hashMap = HashMap<String, Any>()
//                    hashMap[Constants.IS_FOLLOW] = false
                    homeList.clear()
                    friendsList.clear()
                    suggestFriendsList.clear()
//                    newsFeedList.clear()
                    if (snapshot.exists()) {
                        val friend = Friend(suggestFriends.firebase_id!!, false)
                        allUsersRef!!.child(FirebaseAuth.getInstance().currentUser!!.uid)
                            .child(Constants.FRIENDS_REF).child(suggestFriends.firebase_id!!)
                            .setValue(friend)
                        binding!!.progressBar.visibility = View.GONE
                    } else {
                        val friend = Friend(suggestFriends.firebase_id!!, true)
                        allUsersRef!!.child(FirebaseAuth.getInstance().currentUser!!.uid)
                            .child(Constants.FRIENDS_REF).child(suggestFriends.firebase_id!!)
                            .setValue(friend)
                        binding!!.progressBar.visibility = View.GONE
                    }
//                    Toast.makeText(
//                        requireContext(),
//                        "Success",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    CoroutineScope(Dispatchers.Main).launch {
//                        binding!!.progressBar.visibility = View.VISIBLE
//                        getFriends()
//                        delay(3000)
//                        getSuggestedFriends()
//                        binding!!.progressBar.visibility = View.GONE
//                    }
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

    private fun callFollowFirebaseApi(firebaseId: String) {
//        binding!!.progressBar.visibility = View.VISIBLE
        allUsersRef!!
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (i in 0 until friendsList.size) {
                        if (friendsList[i].firebase_id == firebaseId) {
                            friendsList[i]._follow = true
                        }
                    }
                    val hashMap = HashMap<String, Any>()
//                    val friend = Friend(postData.firebase_id, true)
//                    if (snapshot.exists()) {
                    hashMap[Constants.IS_FOLLOW] = true
                    hashMap[Constants.FIREBASE_ID] = firebaseId
                    allUsersRef!!.child(FirebaseAuth.getInstance().currentUser!!.uid)
                        .child(Constants.FRIENDS_REF).child(firebaseId)
                        .setValue(hashMap).addOnCompleteListener {
                        }
                    binding!!.recyclerView.adapter!!.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        val time = Helper().localToGMT()
        notificationRef!!.child(firebaseId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val notification = Notification(
                        FirebaseAuth.getInstance().currentUser!!.uid,
                        firebaseId,
                        Constants.FOLLOW,
                        "Follow",
                        "${sharePref.readString(Constants.FULL_NAME, "")} is now follow you", time,
                        sharePref.readString(Constants.PROFILE_PIC, "")!!,
                        "",
                        false,
                        sharePref.readString(Constants.FULL_NAME, "")!!
                    )
                    notificationRef!!.child(firebaseId).push().setValue(notification)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error ${error.message}", Toast.LENGTH_SHORT)
                        .show()
                }

            })
    }

    override fun onClick(post: Any, type: String, emotionList: List<Pair<String?, Int>>) {
        when (type) {
            Constants.POST -> {
                val postData = post as HomeRecyclerViewItem.SharePostData
                if (postData.firebase_id == FirebaseAuth.getInstance().currentUser!!.uid) {
                    val intent = Intent(requireContext(), ReactDetailActivity::class.java)
                    intent.putExtra(Constants.POST, Gson().toJson(post))
                    intent.putExtra(Constants.EMOTIONS_DATA, Gson().toJson(emotionList))
                    requireContext().startActivity(intent)
                } else if (postData.reaction_type!!.isEmpty()) {
                    val intent = Intent(requireContext(), CameraActivity::class.java)
                    intent.putExtra(Constants.POST, Gson().toJson(post))
                    requireContext().startActivity(intent)
                } else
                    Toast.makeText(requireContext(), "Already reacted", Toast.LENGTH_SHORT).show()
            }
            Constants.REACTION -> {
                val postData = post as HomeRecyclerViewItem.SharePostData
                if (postData.reaction_type!!.isEmpty()) {
                    val intent = Intent(requireContext(), CameraActivity::class.java)
                    intent.putExtra(Constants.POST, Gson().toJson(post))
                    requireContext().startActivity(intent)
                } else
                    Toast.makeText(requireContext(), "Already reacted", Toast.LENGTH_SHORT).show()
            }
            Constants.PROFILE -> {
                val postData = post as HomeRecyclerViewItem.SharePostData
                if (postData.firebase_id != FirebaseAuth.getInstance().currentUser!!.uid) {
                    val intent = Intent(requireContext(), ProfileDetailActivity::class.java)
                    intent.putExtra(Constants.FIREBASE_ID, postData.firebase_id)
                    intent.putExtra(Constants.IS_PRIVATE, postData.reaction_type)
                    intent.putExtra(Constants.IS_FOLLOW, postData.is_follow)
                    requireContext().startActivity(intent)
                } else
                    Toast.makeText(
                        requireContext(),
                        "Please check your profile from profile tab",
                        Toast.LENGTH_SHORT
                    ).show()
//                val sharePostData = post as HomeRecyclerViewItem.SharePostData
//                val transaction = parentFragmentManager.beginTransaction()
//                transaction.replace(R.id.parent, ProfileFragment(sharePostData.firebase_id))
//                transaction.disallowAddToBackStack()
//                transaction.commit()
            }
//            Constants.FOLLOW -> {
//                val postData = post as HomeRecyclerViewItem.SharePostData
//                CoroutineScope(Dispatchers.IO).launch {
//                    callFollowFirebaseApi(postData)
//                }
//            }
        }
    }

    private fun searchFriend() {
        binding!!.searchTool.searchV.setOnQueryTextListener(object :
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
                filter(newText!!)
//                if (newText.isEmpty()) {
//                    searchList.clear()
//                }
                return false
            }
        })
    }

    private fun filter(text: String) {
        // creating a new array list to filter our data.
        val filteredlist: ArrayList<SuggestFriends> = ArrayList()

        // running a for loop to compare elements.
        for (item in suggestFriendsList) {
            // checking if the entered string matched with any item of our recycler view.
            if (item.full_name!!.lowercase(Locale.getDefault())
                    .contains(text.lowercase(Locale.getDefault()))
            ) {
                // if the item is matched we are
                // adding it to our filtered list.
                filteredlist.add(item)
            }
        }
//        if (filteredlist.isEmpty()) {
//            // if no item is added in filtered list we are
//            // displaying a toast message as no data found.
//            Toast.makeText(requireContext(), "No Data Found..", Toast.LENGTH_SHORT).show()
//        } else {
        // at last we are passing that filtered
        // list to our adapter class.
        searchFilterAdapter.filterList(filteredlist)
//        }
    }

    private fun searchAdapterFun() {
//        val linearLayoutManager = LinearLayoutManager(requireContext())
//        binding!!.searchTool.searchRv.layoutManager = linearLayoutManager
        searchFilterAdapter = SearchFilterAdapter(requireContext(), suggestFriendsList, this)
        binding!!.searchTool.searchRv.adapter = searchFilterAdapter
    }

    override fun onResume() {
        super.onResume()
        if (sharePref.readBoolean(Constants.EMOTION_UPDATE, true)) { //stop unnecessary api calling
            homeList.clear()
            friendsList.clear()
            suggestFriendsList.clear()
            newsFeedList.clear()
            CoroutineScope(Dispatchers.Main).launch {
                binding!!.progressBar.visibility = View.VISIBLE
                lastFriendId()
//                delay(600)
                getFriends()
//            getHomeData()
                delay(3000)
                getSuggestedFriends()
                binding!!.progressBar.visibility = View.GONE
            }
            sharePref.writeBoolean(Constants.EMOTION_UPDATE, false)
        }
    }

    override fun onFollow(firebaseId: String, rejected: String) {
        callFollowFirebaseApi(firebaseId)
    }
}