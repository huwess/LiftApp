package com.example.liftapp.bottom_nav.settings_menu

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.liftapp.R
import com.example.liftapp.SignInActivity
import com.example.liftapp.databinding.ActivityEditProfileBinding
import com.example.liftapp.helper.users.UserProfileHelper
import com.example.liftapp.helper.users.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class EditProfileActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var userProfileHelper: UserProfileHelper
    private val userViewModel: UserViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup UI components
        setupDropdown()
        setupInputListeners()  // Add this



        userProfileHelper = UserProfileHelper()
        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        if (currentUser == null || !currentUser.isEmailVerified) {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }
        binding.emailTextView.text = currentUser.email
        fetchUserData(currentUser.uid)

        binding.updateButton.setOnClickListener {
            updateUserData(currentUser.uid)
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
    @SuppressLint("SetTextI18n")
    private fun fetchUserData(userId: String) {
        userProfileHelper.fetchUserData(userId) { user ->
            if (user != null) {
                // Update ViewModel
                userViewModel.apply {
                    setName(user.name)
                    setAge(user.age)
                    setWeight(user.weight)
                    setGender(user.gender)
                    setUnit(user.unit)
                }

                // Update UI from ViewModel
                binding.apply {
                    profileText.text = user.name.first().uppercase()
                    nameTextView.text = user.name
                    name.setText(user.name)
                    age.setText(user.age.toString())
                    weight.setText(user.weight.toString())
                    // Delay setting the dropdown value to ensure options remain visible
                    dropdownField.post {
                        dropdownField.setText(user.gender, false)  // Use `false` to prevent dropdown collapse
                    }
                }
            } else {
                Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUserData(userId: String) {
        // Create update map from ViewModel
        val updates = mapOf(
            "name" to (userViewModel.name.value ?: ""),
            "age" to (userViewModel.age.value ?: 0),
            "weight" to (userViewModel.weight.value ?: 0),
            "gender" to (userViewModel.gender.value ?: ""),
            "unit" to (userViewModel.unit.value ?: 0)
        )

        userProfileHelper.updateUserData(userId, updates) { success, error ->
            if (success) {
                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Update failed: ${error ?: "Unknown error"}",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupDropdown() {
        val items = listOf("Male", "Female")
        val adapter = ArrayAdapter(this, R.layout.list_item, items)
        binding.dropdownField.setAdapter(adapter)

        binding.dropdownField.setOnItemClickListener { _, _, position, _ ->
            val selectedGender = items[position]
            userViewModel.setGender(selectedGender)
        }
    }

    private fun setupInputListeners() {
        binding.name.addTextChangedListener {
            userViewModel.setName(it.toString().trim())
        }

        binding.age.addTextChangedListener {
            val age = it.toString().toIntOrNull() ?: 0
            userViewModel.setAge(age)
        }

        binding.weight.addTextChangedListener {
            val weight = it.toString().toDoubleOrNull() ?: 0.0
            userViewModel.setWeight(weight)
        }
    }
}