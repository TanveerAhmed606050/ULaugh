package com.example.ulaugh.controller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.ulaugh.R
import com.example.ulaugh.databinding.ActivityReactDetailBinding
import com.example.ulaugh.databinding.ActivityUserReactBinding
import com.example.ulaugh.utils.SharePref
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UserReactActivity : AppCompatActivity() {
    private var _binding: ActivityUserReactBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var tokenManager: SharePref
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityUserReactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.included.headerTitle.text = getString(R.string.react)
        binding.included.backBtn.setOnClickListener {
            finish()
        }
    }

}