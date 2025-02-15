package com.example.liftapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.liftapp.databinding.ActivityMainBinding
import com.example.liftapp.fragments.HomeFragment
import com.example.liftapp.fragments.SettingsFragment
import com.google.firebase.auth.FirebaseAuth
import com.example.liftapp.PrefMnager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var prefManager: PrefMnager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)


        setContentView(binding.root)
        prefManager = PrefMnager(this)
        if(prefManager.isFirstTimeLaunch()) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
        }

//        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
        replaceFragment(HomeFragment())

        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null || !currentUser.isEmailVerified) {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }



        // At class level
         val homeFragment = HomeFragment()
         val settingsFragment = SettingsFragment()

    // In setOnItemSelectedListener
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.dashboard -> replaceFragment(homeFragment)
                R.id.settings -> replaceFragment(settingsFragment)
            }
            true
        }


    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .addToBackStack(null)  // Add this line
            .commit()
    }

    // Handle back press properly
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}