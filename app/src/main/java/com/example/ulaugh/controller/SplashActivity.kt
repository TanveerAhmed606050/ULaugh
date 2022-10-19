package com.example.ulaugh.controller

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.example.ulaugh.databinding.ActivitySplashBinding
import com.example.ulaugh.utils.AuthFirebase
import com.example.ulaugh.utils.SharePref
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val delayTime = 1000L
    @Inject
    lateinit var authFirebase: AuthFirebase
//    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Firebase Auth
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        CoroutineScope(Dispatchers.IO).launch {
            delayFun()
        }
    }

    private suspend fun delayFun() {
        delay(delayTime)
        if(authFirebase.auth.currentUser != null){
            startActivity(Intent(this, HomeActivity::class.java))
        }else
            startActivity(Intent(this, LoginActivity::class.java))
        finish()

    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.

    }
}