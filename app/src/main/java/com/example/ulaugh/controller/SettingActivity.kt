package com.example.ulaugh.controller

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.ulaugh.R
import com.example.ulaugh.databinding.ActivitySettingBinding
import com.example.ulaugh.utils.SharePref
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingActivity : AppCompatActivity() {
    private var _binding: ActivitySettingBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var tokenManager: SharePref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.included.headerTitle.visibility = View.VISIBLE
        binding.included2.continueBtn.text = getString(R.string.logout)
        clickListener()
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
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}