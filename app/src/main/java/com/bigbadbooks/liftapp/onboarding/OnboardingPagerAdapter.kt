package com.bigbadbooks.liftapp.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class OnboardingPagerAdapter(fragmentActivity: FragmentActivity,
) : FragmentStateAdapter(fragmentActivity){



    override fun getItemCount(): Int = 3  // Welcome, Essentials, Permissions

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> WelcomeFragment()
            1 -> EssentialsFragment().apply {
                // Add identifier to fragment arguments
                arguments = Bundle().apply {
                    putString("FRAGMENT_TYPE", "ESSENTIALS")
                }
            }
            2 -> PermissionsFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}