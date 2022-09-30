package com.example.ulaugh.controller

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.ulaugh.R
import com.example.ulaugh.databinding.ActivityEditProfileBinding
import com.example.ulaugh.utils.Constants.EMAIL
import com.example.ulaugh.utils.Constants.USERS_REF
import com.example.ulaugh.utils.Constants.FULL_NAME
import com.example.ulaugh.utils.Constants.PROFILE_PIC
import com.example.ulaugh.utils.Constants.USER_NAME
import com.example.ulaugh.utils.DecodeImage
import com.example.ulaugh.utils.Helper
import com.example.ulaugh.utils.SharePref
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EditProfileActivity : AppCompatActivity() {
    @Inject
    lateinit var sharePref: SharePref
    private var _binding: ActivityEditProfileBinding? = null
    private val binding get() = _binding!!
    private val PERMISSION_CODE = 102
    private var email = ""
    private var userName = ""
    private var fullName = ""
    private var imageUri: Uri? = null

    //    private var imagePath: ByteArray? = null
    private lateinit var userProfileRef: DatabaseReference
//    private var compressedImageFile: File? = null

    //    private lateinit var firebaseStorage: FirebaseStorage
//    private lateinit var storageProfilePicReference: StorageReference
    private lateinit var fileRef: StorageReference
    val authFirebase = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        clickEvents()
    }

    private fun initViews() {
        userProfileRef =
            FirebaseDatabase.getInstance().getReference(USERS_REF)
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
        binding.included.backBtn.setOnClickListener {
            finish()
        }
        val url = sharePref.readString(PROFILE_PIC, "")
        Glide.with(this)
            .load(url)
            .centerCrop()
            .fitCenter()
            .thumbnail()
            .placeholder(R.drawable.seokangjoon)
            .into(binding.profileIv)
//        if (url!!.isNotEmpty())
//            Glide.with(this).load(url).into(binding.profileIv)
        binding.nameTv.text = sharePref.readString(FULL_NAME, "")
        binding.statusTv.text = "@${sharePref.readString(USER_NAME, "")}"
        binding.userName.setText(sharePref.readString(USER_NAME, ""))
        binding.fullName.setText(sharePref.readString(FULL_NAME, ""))
        binding.emailEt.setText(sharePref.readString(EMAIL, ""))
        binding.included.headerTitle.text = getText(R.string.edit_profile)
        binding.included2.continueBtn.text = getString(R.string.save)
    }

    private fun clickEvents() {
        binding.editIv.setOnClickListener {
            if (galleryPermissionGranted()) {
//                CoroutineScope(Dispatchers.IO).launch {
                openGalleryForImage()
//                    binding.profileIv.setImageURI(imageUri)
//                }
            }
        }

        binding.included2.continueBtn.setOnClickListener {
            if (isValidateFields()) {
                it.isEnabled = false
                binding.txtError.text = ""
                binding.progressBar.visibility = View.VISIBLE
                binding.included2.continueBtn.isEnabled = false
                if (imageUri != null)
                    updateProfilePic()
                else
                    updateRealData()
            } else
                binding.txtError.text = "Please provide credential"
        }
    }

    private fun updateProfilePic() {
        fileRef = FirebaseStorage.getInstance().getReference(PROFILE_PIC)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("pic" + ".png")
        fileRef.putFile(imageUri!!).addOnSuccessListener { task ->
            if (task.metadata != null) {
                val taskUrl = task.metadata!!
                    .reference!!.downloadUrl
//                val taskUrl = fileRef.downloadUrl
//                val userMap = HashMap<String, Any>()
//                userMap[PROFILE_PIC] = taskUrl
//                if (email.isNotEmpty())
//                    userMap[EMAIL] = email
//                if (userName.isNotEmpty())
//                    userMap[USER_NAME] = userName
//                if (fullName.isNotEmpty())
//                    userMap[FULL_NAME] = fullName
//                databaseReference.child(PROFILE_PIC).setValue(userMap).addOnCompleteListener {
//                    if (it.isSuccessful){
//                        Toast.makeText(this, "Update successfully", Toast.LENGTH_SHORT).show()
//                        finish()
//                    }
//                    else if (it.isCanceled)
//                        Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show()
//                }
                taskUrl.addOnSuccessListener { uri ->
//                    val downloadUri = it.toString()
                    val userMap = HashMap<String, Any>()
                    if (userName.isNotEmpty()) {
                        userMap[USER_NAME] = userName
                        sharePref.writeString(
                            USER_NAME,
                            userName
                        )
                    }
                    if (fullName.isNotEmpty()) {
                        userMap[FULL_NAME] = fullName
                        sharePref.writeString(
                            FULL_NAME,
                            fullName
                        )
                    }
                    if (email.isNotEmpty()) {
                        userMap[EMAIL] = email
                        sharePref.writeString(
                            EMAIL,
                            email
                        )
                    }
//                    Log.d(TAG, "updateProfile: ${taskUrl.result}")
                    userMap[PROFILE_PIC] = uri.toString()
                    userProfileRef.updateChildren(userMap)
                    sharePref.writeString(
                        PROFILE_PIC,
                        uri.toString()
                    )
//                    if (email.isNotEmpty())
//                        userMap[EMAIL] = email
//                    if (userName.isNotEmpty())
//                        userMap[USER_NAME] = userName
//                    if (fullName.isNotEmpty())
//                        userMap[FULL_NAME] = fullName
//                    userMap[FIREBASE_ID] = authFirebase.currentUser!!.uid
/*                    userProfileRef
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                userProfileRef
                                    .child(
                                        PROFILE_PIC
                                    ).setValue(taskUrl.result.toString())
                                sharePref.writeString(
                                    PROFILE_PIC,
                                    snapshot.child(PROFILE_PIC).value.toString()
                                )
                                if (userName.isNotEmpty()) {
                                    userProfileRef
                                        .child(
                                            USER_NAME
                                        ).setValue(userName)
                                    sharePref.writeString(
                                        USER_NAME,
                                        userName
                                    )
                                }
                                if (fullName.isNotEmpty()) {
                                    userProfileRef
                                        .child(
                                            FULL_NAME
                                        ).setValue(fullName)
                                    sharePref.writeString(
                                        FULL_NAME,
                                        fullName
                                    )
                                }
                                if (email.isNotEmpty()) {
                                    userProfileRef
                                        .child(
                                            EMAIL
                                        ).setValue(email)
                                    sharePref.writeString(
                                        EMAIL,
                                        email
                                    )
                                }
                                sharePref.writeString(
                                    PROFILE_PIC,
                                    snapshot.child(PROFILE_PIC).value.toString()
                                )
                                Toast.makeText(
                                    this@EditProfileActivity,
                                    "Update successfully",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                finish()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                        })*/
                }
            } else
                Toast.makeText(this, "Not updated successfully ${task.error}", Toast.LENGTH_SHORT)
                    .show()
            binding.progressBar.visibility = View.GONE
            binding.included2.continueBtn.isEnabled = true
        }
    }

    private fun updateRealData() {
        userProfileRef
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (userName.isNotEmpty()) {
                        userProfileRef
                            .child(
                                USER_NAME
                            ).setValue(userName)
                        sharePref.writeString(
                            USER_NAME,
                            userName
                        )
                    }
                    if (fullName.isNotEmpty()) {
                        userProfileRef
                            .child(
                                FULL_NAME
                            ).setValue(fullName)
                        sharePref.writeString(
                            FULL_NAME,
                            fullName
                        )
                    }
                    if (email.isNotEmpty()) {
                        userProfileRef
                            .child(
                                EMAIL
                            ).setValue(email)
                        sharePref.writeString(
                            EMAIL, email
                        )
                    }

                    Toast.makeText(
                        this@EditProfileActivity,
                        "Update successfully",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    finish()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun isValidateFields(): Boolean {
        userName = binding.userName.text.toString()
        fullName = binding.fullName.text.toString()
        email = binding.emailEt.text.toString()
        if (userName.isNotEmpty() || fullName.isNotEmpty() || (email.isNotEmpty() && Helper.isValidEmail(
                email
            )) || imageUri != null
        ) {
            return true
        }
        return false
    }

    private fun openGalleryForImage() {
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        contract.launch(photoPickerIntent)
    }

    private val contract =
        this.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                imageUri = it.data?.data!!

                CoroutineScope(Dispatchers.IO).launch {
                    imageUri = DecodeImage.compressImage(imageUri!!, this@EditProfileActivity)
//                    val imageUrl = getRealPathFromURI(imageUri!!, this@EditProfileActivity)
//                    val file = File(imageUrl!!)
//                    if (file.length() > 550000) {
//                        compressedImageFile = Compressor.compress(this@EditProfileActivity, file) {
//                            resolution(400, 350)
//                            quality(100)
//                            format(Bitmap.CompressFormat.PNG)
////                        size(2_097_152) // 2 MB
//                        }
//                        imageUri = Uri.fromFile(compressedImageFile)
//                    }
                }
                binding.profileIv.setImageURI(imageUri)
            } else
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
        }

    private fun galleryPermissionGranted(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) ==
            PackageManager.PERMISSION_DENIED
        ) {
            //permission denied
            val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            //show popup to request runtime permission
            requestPermissions(permissions, PERMISSION_CODE)
            false
        } else
            true

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
                    CoroutineScope(Dispatchers.IO).launch {
                        openGalleryForImage()
                    }
                } else {
                    Toast.makeText(this, "Gallery Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

//    private fun enableViews(vararg views: View) {
//        for (v in views) {
//            if (views.size == views.size - 1)
//                v.isClickable = true
//            else
//                v.isEnabled = true
//        }
//    }
}