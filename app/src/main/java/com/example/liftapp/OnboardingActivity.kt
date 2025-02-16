package com.example.liftapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.example.liftapp.onboarding.EssentialsFragment
import com.example.liftapp.onboarding.OnboardingPagerAdapter
import com.example.liftapp.onboarding.PermissionsFragment
import com.example.liftapp.onboarding.WelcomeFragment
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import com.example.liftapp.helper.users.UserProfileHelper
import com.example.liftapp.helper.users.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var prefMnager: PrefMnager
    private lateinit var userProfileHelper: UserProfileHelper
    private lateinit var nextButton: Button
    private val userViewModel: UserViewModel by viewModels()


    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            navigateToMainActivity()
        } else {
            // Open app settings if permission is denied
            openAppSettings()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_onboarding)

        // Check if user is authenticated
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            // Redirect to login/register
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }

        userProfileHelper = UserProfileHelper()
        nextButton = findViewById<Button>(R.id.nextButton)
        val backButton = findViewById<Button>(R.id.backButton)

        prefMnager = PrefMnager(this)

        val fragments = listOf(
            WelcomeFragment(),
            EssentialsFragment(),
            PermissionsFragment(),
        )
        viewPager = findViewById(R.id.viewPager)
        viewPager.isUserInputEnabled = false
        // Set up the adapter for the ViewPager
        val onboardingAdapter = OnboardingPagerAdapter(this)
        viewPager.adapter = onboardingAdapter

        // Initialize and attach the DotsIndicator
        val indicator = findViewById<DotsIndicator>(R.id.dots_indicator)
        indicator.attachTo(viewPager)

        if(viewPager.currentItem == 0) {
            backButton.isEnabled = false
            backButton.isVisible = false
        }
        userViewModel.run {
            name.observe(this@OnboardingActivity) { updateButtonState() }
            age.observe(this@OnboardingActivity) { updateButtonState() }
            weight.observe(this@OnboardingActivity) { updateButtonState() }
            gender.observe(this@OnboardingActivity) { updateButtonState() }
            unit.observe(this@OnboardingActivity) { updateButtonState() }
        }


        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateButtonState()
                // Show back button only after the first page
                backButton.isVisible = position > 0
                backButton.isEnabled = position > 0


                // Change "Next" button text on the last page
                if (position == fragments.size - 1) {
                    nextButton.text = getString(R.string.allow)
                } else {
                    nextButton.text = getString(R.string.next) // Reset text for other pages
                }
            }
        })
        nextButton.setOnClickListener {
            // Handle "Next" button click
            if (viewPager.currentItem < fragments.size - 1) {
                viewPager.currentItem++
            } else {
                // Set the flag to indicate onboarding completion
                prefMnager.setFirstTimeLaunch(false)



                // If last page, save data and request camera permission
                saveUserDataAndRequestPermission()
            }

            if (viewPager.currentItem == fragments.size - 1) {
                nextButton.text = getString(R.string.allow)
            }
        }
        backButton.setOnClickListener {
            if(viewPager.currentItem > 0) {
                viewPager.currentItem--
            }
        }
    }

    override fun onResume() {
        super.onResume()
//        if(viewPager.currentItem != 1) {
//            nextButton.isEnabled = true
//            nextButton.isVisible = true
//        }
    }
    private fun clearMemory() {
        viewPager.adapter = null
    }

    override fun onDestroy() {
        clearMemory()
        super.onDestroy()
    }

    private fun updateButtonState() {
        val isValid = when (viewPager.currentItem) {
            1 -> {  // Essentials fragment position
                userViewModel.name.value?.isNotBlank() == true &&
                        userViewModel.age.value?.let { it > 0 } == true &&
                        userViewModel.weight.value?.let { it > 0 } == true &&
                        userViewModel.gender.value != null
            }
            else -> true
        }

        nextButton.isEnabled = isValid
        nextButton.alpha = if (isValid) 1f else 0.5f
    }


    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            navigateToMainActivity()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun saveUserDataAndRequestPermission() {
        val userViewModel: UserViewModel by viewModels()

        Log.d("OnboardingActivity", "Name: ${userViewModel.name}, Age: ${userViewModel.age}, Unit: ${userViewModel.unit}, Gender: ${userViewModel.gender}, Weight: ${userViewModel.weight}")
        userViewModel.run {
            userProfileHelper.saveUserData(
                name.value ?: "",
                age.value ?: 0,
                weight.value ?: 0,
                gender.value ?: "",
                unit.value ?: 0
            ) { success, error ->
                // ... handle result ...
                if (success) {
                    result("Success", error)
                    requestCameraPermission()
                } else {
                    result("Error", error)
                }
            }
        }
    }
    private fun result(result: String, error: String?) {
        if(result == "Success") {
            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error saving data: $error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }
}


class PrefMnager(context: Context) {
    private val pref: SharedPreferences =
        context.getSharedPreferences("MyPref", Context.MODE_PRIVATE)

    fun isFirstTimeLaunch(): Boolean {
        return pref.getBoolean("isFirstTimeLaunch", true)
    }

    fun setFirstTimeLaunch(isFirstTime: Boolean) {
        pref.edit().putBoolean("isFirstTimeLaunch", isFirstTime).apply()
    }
}