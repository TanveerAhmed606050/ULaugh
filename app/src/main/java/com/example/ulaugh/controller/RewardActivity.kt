package com.example.ulaugh.controller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.example.ulaugh.R
import com.example.ulaugh.adapter.RewardAdapter
import com.example.ulaugh.databinding.ActivityRewardBinding
import com.example.ulaugh.utils.SharePref
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RewardActivity : AppCompatActivity() {
    private var _binding: ActivityRewardBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var tokenManager: SharePref
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRewardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initValue()
        setAdapter()
    }

    private fun setAdapter() {
        val adapter = RewardAdapter(this)
        binding.rv.adapter = adapter
    }

    private fun initValue() {
        binding.included.headerTitle.visibility = View.VISIBLE
        binding.included.headerTitle.text = getText(R.string.reward)
        binding.included.backBtn.setOnClickListener {
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}