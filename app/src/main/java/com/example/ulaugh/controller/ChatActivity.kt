package com.example.ulaugh.controller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.ulaugh.databinding.ActivityChatBinding
import com.example.ulaugh.utils.SharePref
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {
    @Inject
    lateinit var sharePref: SharePref
    private var _binding: ActivityChatBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        clickListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun clickListener(){
        binding.backBtn.setOnClickListener {
            finish()
        }
        binding.menuIv.setOnClickListener {  }
    }
}