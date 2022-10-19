package com.example.ulaugh.controller

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.example.ulaugh.R
import com.example.ulaugh.adapter.ViewPagerAdapter
import com.example.ulaugh.databinding.ActivityHomeBinding
import com.example.ulaugh.model.PostShareInfo
import com.example.ulaugh.utils.Constants
import com.example.ulaugh.utils.DecodeImage
import com.example.ulaugh.utils.Helper
import com.example.ulaugh.utils.SharePref
import com.github.ybq.android.spinkit.SpinKitView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppCompatActivity(), View.OnKeyListener {
    private var _binding: ActivityHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var photoView: ImageView
    private lateinit var tagEt: EditText
    private lateinit var tagList: String
    private lateinit var descriptionEt: EditText
    private lateinit var videoView: VideoView
    private lateinit var txt: TextView
    private lateinit var chooseImage: ImageView
    private val PERMISSION_CODE = 102
    private var dialog: BottomSheetDialog? = null
    private var progressBar: SpinKitView? = null
    private var postBtn: AppCompatButton? = null
    private var container_ll: LinearLayout? = null
    private var imageUri: Uri? = null
    private var mediaTypeRaw = ""

    //    private val storagePath = "All_Image_Uploads/"
    var postShareStorageRef: StorageReference? = null
    private lateinit var postShareDbRef: DatabaseReference
    var description: String? = null

    //    val authFirebase = Firebase.auth
    var userName = ""
    var fullName = ""
//    var reaction: ReactionDetails? = null

    //    var tags: String? = null
    @Inject
    lateinit var sharePref: SharePref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        init()
        CoroutineScope(Dispatchers.IO).launch {
            fetchToken()
        }
        setTabLayout()
        sharePref.writeBoolean(Constants.EMOTION_UPDATE, true)//stop api calling
    }

    private fun init() {
        // Assign FirebaseDatabase instance with root database name.
        postShareDbRef = FirebaseDatabase.getInstance().reference.child(Constants.POST_SHARE_REF)
        postShareStorageRef =
            FirebaseStorage.getInstance().reference.child(Constants.POST_SHARE_REF)
        clickEvents()
//        userId = FirebaseAuth.getInstance().currentUser!!.uid
        userName = sharePref.readString(Constants.USER_NAME, "").toString()
        fullName = sharePref.readString(Constants.FULL_NAME, "").toString()
    }

    private fun clickEvents() {
        binding.editView.setOnClickListener { customDialog() }
    }

    private fun sharePost(tagsList: String, time: String) {
//        if (mediaTypeRaw.startsWith("image"))
//            storageRef!!.child(FirebaseAuth.getInstance().currentUser!!.uid)
//                .child("${System.currentTimeMillis()}.png")
//        else
//            storageRef!!.child(FirebaseAuth.getInstance().currentUser!!.uid)
//                .child("${System.currentTimeMillis()}.mp4")
        val systemTime = System.currentTimeMillis().toString()
        postShareStorageRef!!.child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("${systemTime}.png")
            .putFile(imageUri!!).addOnSuccessListener { taskSnapshot ->
                if (taskSnapshot.metadata != null) {
                    val taskUrl = taskSnapshot!!.metadata!!.reference!!.downloadUrl
//                val taskUrl = storageRef!!.child(FirebaseAuth.getInstance().currentUser!!.uid).child("${systemTime}.png").downloadUrl
                    taskUrl.addOnSuccessListener {
                        val imageUrl = it.toString()
//                    val imageUrl = taskUrl.result.toString()
                        val shareInfo = PostShareInfo(
                            FirebaseAuth.getInstance().currentUser!!.uid,
                            imageUrl,
                            description!!,
                            time,
                            userName,
                            fullName,
                            tagsList,
                            Constants.REACTION,
                            mediaTypeRaw,
                            sharePref.readString(Constants.PROFILE_PIC, "")!!
                        )
                        val imageUploadId = postShareDbRef.push().key

                        postShareDbRef.child(FirebaseAuth.getInstance().currentUser!!.uid)
                            .child(imageUploadId!!).setValue(shareInfo)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
//                                databaseReference.child(FirebaseAuth.getInstance().currentUser!!.uid)
//                                    .child(imageUploadId)
//                                    .setValue(shareInfo)
//                                    .child(Constants.REACTION)
//                                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
//                                    .setValue("")
                                } else
                                    Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show()
                                dialog!!.dismiss()
                            }.addOnFailureListener {
                                Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
                                progressBar?.visibility = View.GONE
                            }
                    }.addOnFailureListener {
                        Toast.makeText(this, "Failed: ${it.message.toString()}", Toast.LENGTH_SHORT)
                            .show()
                        progressBar?.visibility = View.GONE
                    }.addOnCanceledListener {
                        Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show()
                        progressBar?.visibility = View.GONE
                    }
                } else
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            }.addOnCanceledListener {
                dialog!!.dismiss()
                Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show()
                progressBar?.visibility = View.GONE
            }.addOnFailureListener {
                Toast.makeText(this, "Failure: ${it.message.toString()}", Toast.LENGTH_SHORT).show()
                dialog!!.dismiss()
                progressBar?.visibility = View.GONE
            }.addOnProgressListener {
                progressBar?.visibility = View.VISIBLE
            }
    }

    private fun setTabLayout() {
        binding.tabLayout.addTab(
            binding.tabLayout.newTab().setText("Home").setIcon(R.drawable.home)
        )
        binding.tabLayout.addTab(
            binding.tabLayout.newTab().setText("Chat").setIcon(R.drawable.message_circle_dots)
        )
        binding.tabLayout.addTab(
            binding.tabLayout.newTab().setText("Notification").setIcon(R.drawable.bell)
        )
        binding.tabLayout.addTab(
            binding.tabLayout.newTab().setText("Profile").setIcon(R.drawable.user_logo)
        )
        binding.tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        val pagerAdapter = ViewPagerAdapter(supportFragmentManager)

        binding.viewPager.offscreenPageLimit = 3
        binding.viewPager.adapter = pagerAdapter
        binding.tabLayout.getTabAt(0)?.select()

        var tabIconColor = ContextCompat.getColor(this@HomeActivity, R.color.white)
        binding.tabLayout.getTabAt(0)!!.icon?.setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN)
        tabIconColor = ContextCompat.getColor(this@HomeActivity, R.color.light_purple)
        binding.tabLayout.getTabAt(1)!!.icon?.setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN)
        binding.tabLayout.getTabAt(2)!!.icon
            ?.setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN)
        binding.tabLayout.getTabAt(3)!!.icon
            ?.setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN)

        binding.viewPager.addOnPageChangeListener(TabLayoutOnPageChangeListener(binding.tabLayout))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val tabIconColor = ContextCompat.getColor(this@HomeActivity, R.color.white)
                tab.icon!!.setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN)
                binding.viewPager.currentItem = tab.position
                if (tab.position != 0) {
                    binding.editView.visibility = View.GONE
//                    binding.editIv.visibility = View.GONE
                } else
                    binding.editView.visibility = View.VISIBLE

            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val tabIconColor = ContextCompat.getColor(this@HomeActivity, R.color.light_purple)
                tab.icon!!.setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                val tabIconColor = ContextCompat.getColor(this@HomeActivity, R.color.white)
                tab.icon!!.setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN)
            }
        })
    }

//    private fun getWindowHeight(): Int {
//        // Calculate window height for fullscreen use
//        val displayMetrics = DisplayMetrics()
//        windowManager.defaultDisplay.getMetrics(displayMetrics)
//        return displayMetrics.heightPixels
//    }

    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = (resources.displayMetrics.heightPixels * 0.90).toInt()
        bottomSheet.layoutParams = layoutParams
    }

    private fun customDialog() {
        dialog = BottomSheetDialog(this)
        dialog!!.setContentView(R.layout.custom_dialog)
        postBtn = dialog!!.findViewById(R.id.continue_btn)
        descriptionEt = dialog!!.findViewById(R.id.description_tv)!!
        progressBar = dialog!!.findViewById(R.id.progress_bar)
        photoView = dialog!!.findViewById(R.id.photo_iv)!!
        videoView = dialog!!.findViewById(R.id.video_v)!!
        chooseImage = dialog!!.findViewById(R.id.choose_iv)!!
        container_ll = dialog!!.findViewById(R.id.tags_container)
        txt = dialog!!.findViewById(R.id.textView22)!!
        tagEt = dialog!!.findViewById(R.id.tag_tv)!!

        postBtn?.text = getText(R.string.post)

        postBtn?.setOnClickListener {
            Helper.hideKeyboard(it)
            description = descriptionEt.text.toString()
            tagList = tagEt.text.toString()
            if (description == null || tagList.isEmpty() || imageUri == null) {
                Toast.makeText(this, "Required Fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val time = Helper().localToGMT()
            CoroutineScope(Dispatchers.IO).launch {
//                reaction = ReactionDetails(FirebaseAuth.getInstance().currentUser!!.uid, "")
                sharePost(tagList, time)
            }
        }

        chooseImage.setOnClickListener {
            askGalleryPermission()
        }
//        tagEt.setOnKeyListener(this)
        dialog!!.setOnShowListener {

            val bottomSheetDialog = it as BottomSheetDialog
            val parentLayout =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { parent ->
                val behaviour = BottomSheetBehavior.from(parent)
                setupFullHeight(parent)
                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        dialog!!.show()
    }

    private val contract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                chooseImage.visibility = View.GONE
                txt.visibility = View.GONE

                val data: Intent = it.data!!
                imageUri = data.data
                mediaTypeRaw = contentResolver.getType(imageUri!!)!!
                if (mediaTypeRaw.startsWith("image")) {
                    photoView.setImageURI(imageUri)
//                    mediaTypeRaw = "image"
                    CoroutineScope(Dispatchers.IO).launch {
                        imageUri = DecodeImage.compressImage(imageUri!!, this@HomeActivity)
                    }
                }
                if (mediaTypeRaw.startsWith("video"))
//                    mediaTypeRaw = "video"
                    videoChooser(imageUri!!)
            }
        }

    private fun askGalleryPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) ==
            PackageManager.PERMISSION_DENIED
        ) {
            //permission denied
            val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            //show popup to request runtime permission
            requestPermissions(permissions, PERMISSION_CODE)
        } else {
            openGalleryForImage()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<String>,
        @NonNull grantResults: IntArray,
    ) {
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission from popup granted
                    openGalleryForImage()
                } else {
                    Toast.makeText(this, "Gallery Permission denied", Toast.LENGTH_SHORT).show()
                    //permission from popup denied
                }
            }
        }
    }

    private fun fetchToken() {
        // [START fcm_runtime_enable_auto_init]
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(Constants.TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
//cfNtuWZzTVGBYI7CFXuKq6:APA91bFYbHB64p9vkfyj6U3_Ii8YLDFnqUTja4q9uyNtk6GrwIqWi7L-RmU-AJI_nrH__gZsFkOVEw4uflzaOzifIpuA_1XDgHGGgeqDjJztbZpEd1k6QTgZ4rAp_j9CRradIqQ9MTsB
            // Get new FCM registration token
            val token = task.result
            FirebaseDatabase.getInstance().reference.child(Constants.USERS_REF)
                .child(FirebaseAuth.getInstance().currentUser!!.uid).child(Constants.MESSAGE_TOKEN)
                .setValue(token)
            // Log and toast
            val msg = getString(R.string.msg_token_fmt, token)
            Log.d(Constants.TAG, msg)
//            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })
        // [END fcm_runtime_enable_auto_init]
    }

    private fun openGalleryForImage() {
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "*/*"
        photoPickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
        contract.launch(photoPickerIntent)
    }

    private fun videoChooser(uri: Uri) {
        photoView.visibility = View.INVISIBLE
        videoView.visibility = View.VISIBLE
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setVideoPath(uri.toString())
        videoView.setMediaController(mediaController)
        videoView.setOnPreparedListener { mp: MediaPlayer ->
//            progressBar.setVisibility(View.GONE)
            mp.start()
            mp.isLooping = true
        }
        videoView.setOnCompletionListener { mp: MediaPlayer? -> }

    }

//    private fun setTagsText(tag: ArrayList<String>) {
//        val layoutParams = LinearLayout.LayoutParams(
//            LinearLayout.LayoutParams.WRAP_CONTENT,
//            LinearLayout.LayoutParams.WRAP_CONTENT
//        )
//        layoutParams.setMargins(4, 0, 0, 0)
//        container_ll?.visibility = View.VISIBLE
//        for (i in 0 until tag.size) {
//            val tagLl = LinearLayout(this)
//            tagLl.layoutParams = layoutParams
//            tagLl.orientation = LinearLayout.HORIZONTAL
//            tagLl.gravity = Gravity.CENTER_VERTICAL
//            tagLl.background = getDrawable(R.drawable.purple_rc)
//            tagLl.id = R.id.tag_tv + i
//
//            //textView
//            val tagTv = TextView(this)
//            tagTv.layoutParams = layoutParams
//            tagTv.text = tag[0]
//            tagTv.textSize = 14f
//            tagTv.id = R.id.id_tv + i
//            tagTv.setPadding(4, 4, 4, 4)
//            tagTv.gravity = Gravity.CENTER_VERTICAL
//            tagTv.setTextColor(getColor(R.color.white))
////            tagTv.text = tagList[i]
//
//            //ImageView
//            val cancelIv = ImageView(this)
//            cancelIv.layoutParams = LinearLayout.LayoutParams(18, 18)
//            cancelIv.setImageResource(R.drawable.bell)
//            tagLl.addView(tagTv)
//            tagLl.addView(cancelIv)
//
//            container_ll?.addView(tagLl)
//
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onKey(view: View?, p: Int, event: KeyEvent?): Boolean {
//        if (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
//            if (tagEt.text.toString().contains("#"))
//                tagList.add(tagEt.text.trim().toString())
//            else
//                tagList.add("#" + tagEt.text.trim().toString())
//            setTagsText(tagList)
//            tagEt.setText("")
//            Toast.makeText(this, tagEt.text.trim(), Toast.LENGTH_SHORT).show()
        return true
//        }
//        when (event?.keyCode) {
//            (KeyEvent.KEYCODE_ENTER) -> {
//                tagList.add(tagEt.text.trim().toString())
//                setTagsText(tagList)
//                tagEt.setText("")
//                Toast.makeText(this, tagEt.text.trim(), Toast.LENGTH_SHORT).show()
//                return true
//            }
//        }
//        return false
    }
}