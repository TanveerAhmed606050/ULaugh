package com.example.ulaugh.controller

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.ulaugh.R
import com.example.ulaugh.databinding.ActivitySettingBinding
import com.example.ulaugh.utils.Constants
import com.example.ulaugh.utils.SharePref
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingActivity : AppCompatActivity() {
    private var _binding: ActivitySettingBinding? = null
    private val binding get() = _binding!!
    private lateinit var databaseReference: DatabaseReference
    private var userId = ""

    @Inject
    lateinit var sharePref: SharePref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        binding.included.headerTitle.visibility = View.VISIBLE
        binding.included2.continueBtn.text = getString(R.string.logout)
        clickListener()
    }

    private fun init() {
        userId = FirebaseAuth.getInstance().currentUser!!.uid
        databaseReference = FirebaseDatabase.getInstance().reference.child(Constants.USERS_REF)
        binding.privateSwitch.isChecked = sharePref.readBoolean(Constants.IS_PRIVATE, true)
    }

    private fun clickListener() {
        binding.included.backBtn.setOnClickListener {
            finish()
        }
        binding.rewardRl.setOnClickListener {
            startActivity(Intent(this, RewardActivity::class.java))
        }
        binding.editProRl.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
        binding.changePhRl.setOnClickListener {
            startActivity(Intent(this, ChangeContactActivity::class.java))
        }
        binding.included2.continueBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        binding.privateSwitch.setOnCheckedChangeListener { compoundButton, b ->

            databaseReference.child(userId).child(Constants.IS_PRIVATE)
                .setValue(compoundButton.isChecked)
            sharePref.writeBoolean(
                Constants.IS_PRIVATE,
                compoundButton.isChecked,
            )
//                databaseReference.child(userId).addListenerForSingleValueEvent(object :ValueEventListener{
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        databaseReference.child()
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {
//                        TODO("Not yet implemented")
//                    }
//
//                })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}