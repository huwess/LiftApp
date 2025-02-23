package com.example.liftapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.liftapp.databinding.ActivityMainBinding
import com.example.liftapp.bottom_nav.HomeFragment
import com.example.liftapp.bottom_nav.SettingsFragment
import com.google.firebase.auth.FirebaseAuth
import com.example.liftapp.helper.users.UserProfileHelper
import com.example.liftapp.helper.users.UserViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var prefManager: PrefMnager

    private lateinit var userProfileHelper: UserProfileHelper
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        userProfileHelper = UserProfileHelper()

        setContentView(binding.root)
        prefManager = PrefMnager(this)
        if(prefManager.isFirstTimeLaunch()) {
            if (currentUser != null) {
                userProfileHelper.checkUserExists(currentUser.uid) { exists ->
                    if (!exists) {
                        startActivity(Intent(this, OnboardingActivity::class.java))
                        Log.d("UserCheck", "User does not exist.")
                        finish()

                    }
                }
            }

        }

//        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
        replaceFragment(HomeFragment())




        if (currentUser == null || !currentUser.isEmailVerified) {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        } else {
            userViewModel.setEmail(currentUser.email)
            fetchUserData(currentUser.uid)
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

    private fun fetchUserData(userId: String) {
        Log.d("MainActivity", "Calling fetchUserData for userId: $userId")

        userProfileHelper.fetchUserData(userId) { user ->
            if (user != null) {
                Log.d("MainActivity", "User data received: $user")

                userViewModel.setName(user.name)
                userViewModel.setAge(user.age)
                userViewModel.setWeight(user.weight)
                userViewModel.setGender(user.gender)
                userViewModel.setUnit(user.unit)
            } else {
                Log.e("MainActivity", "User data is null")
                Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
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