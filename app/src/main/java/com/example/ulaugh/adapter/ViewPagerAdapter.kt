package com.example.ulaugh.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.ulaugh.fragment.*

class ViewPagerAdapter(fragment: FragmentManager) : FragmentPagerAdapter(fragment, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                HomeFragment()
            }
            1 ->{
                ChatFragment()
            }
            2 -> {
                NotificationFragment()
            }
            3 -> {
                ProfileFragment()
            }
            else -> getItem(position)
        }
    }
    override fun getCount(): Int {
        return 4
    }

}