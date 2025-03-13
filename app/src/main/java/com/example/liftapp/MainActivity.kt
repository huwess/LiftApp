package com.example.liftapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.liftapp.databinding.ActivityMainBinding
import com.example.liftapp.bottom_nav.HomeFragment
import com.example.liftapp.bottom_nav.SettingsFragment
import com.example.liftapp.bottom_nav.fab_add.ExerciseActivity
import com.example.liftapp.exercise.ExerciseCarouselFragment
import com.example.liftapp.helper.audio.TextToSpeechHelper
import com.google.firebase.auth.FirebaseAuth
import com.example.liftapp.helper.users.UserProfileHelper
import com.example.liftapp.helper.users.UserViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var prefManager: PrefMnager

    private lateinit var userProfileHelper: UserProfileHelper
    private lateinit var ttsHelper: TextToSpeechHelper
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        userProfileHelper = UserProfileHelper()
        ttsHelper = TextToSpeechHelper(this)
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


        replaceFragment(ExerciseCarouselFragment())




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
        // Adding the OnBackPressedCallback
        onBackPressedDispatcher.addCallback(this) {
            if (supportFragmentManager.backStackEntryCount > 1) {
                replaceFragment(homeFragment)
            } else {
                moveTaskToBack(true)
            }
        }

        binding.fabButton.setOnClickListener{
//            val intent = Intent(this, ExerciseActivity::class.java)
//            startActivity(intent)
            showExercisePreparationDialog()
        }


    }

    private fun fetchUserData(userId: String) {
        Log.d("MainActivity", "Calling fetchUserData for userId: $userId")

        userProfileHelper.fetchUserData() { user ->
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


    fun replaceFragment(fragment: Fragment) {
        // Check if the fragment is ExerciseCarouselFragment
        if (fragment is ExerciseCarouselFragment) {
            binding.bottomNavigationView.visibility = View.GONE // Hide the bottom navigation bar
            binding.fabButton.visibility = View.GONE // Hide the FAB when ExerciseCarouselFragment is shown
        } else {
            binding.bottomNavigationView.visibility = View.VISIBLE // Show the bottom navigation bar for other fragments
            binding.fabButton.visibility = View.VISIBLE // Show the FAB for other fragments
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .addToBackStack(null)  // Add this line
            .commit()
    }




    @SuppressLint("MissingInflatedId")
    private fun showExercisePreparationDialog() {
        val builder = AlertDialog.Builder(this, R.style.fullscreenalert)
        val mview = layoutInflater.inflate(R.layout.exercise_prepration_dialog, null)
        builder.setView(mview)
        val dialog = builder.create()

        var unit : Int = 0
        var unitSelected = false // Track if a unit has been selected

        val items = listOf("kg", "lb")
        val adapter = ArrayAdapter(this, R.layout.list_item, items)
        val dropdownField = mview.findViewById<AutoCompleteTextView>(R.id.dropdown_field)
        val inputLayout = mview.findViewById<TextInputLayout>(R.id.input_layout)
        val weightInput = mview.findViewById<TextInputEditText>(R.id.dumbbell_weight)
        val backButton = mview.findViewById<Button>(R.id.back)
        val nextButton = mview.findViewById<Button>(R.id.next)
        dropdownField.setAdapter(adapter)

        dropdownField.setOnItemClickListener { adapterView, view, i, l -> }

        dropdownField.setOnClickListener {
            hideKeyboard(dropdownField)
        }

        dropdownField.setOnItemClickListener { _, _, position, _ ->
            unitSelected = true
            val selectedUnit = items[position]
            unit = if (selectedUnit == "kg") 0 else 1

            // Check both conditions before enabling the button
            val isNotEmpty = !weightInput.text.isNullOrEmpty()
            nextButton.isEnabled = isNotEmpty && unitSelected
            nextButton.setBackgroundColor(if (nextButton.isEnabled) getColor(R.color.calm) else getColor(R.color.gray))
            nextButton.alpha = if (nextButton.isEnabled) 1f else 0.5f
        }


        dialog.setOnCancelListener {
            ttsHelper.speakText("Exercise Cancelled.")
        }



        dialog.show()

        dialog.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            // Optional: Remove any background that might limit size
            setBackgroundDrawableResource(android.R.color.white)
        }

        val imageView = mview.findViewById<ImageView>(R.id.gif_animation)

        Glide.with(mview).asGif().load(R.drawable.dumbbellpress).into(imageView)

        val inputContainer = mview.findViewById<LinearLayout>(R.id.input_container)
        nextButton.setBackgroundColor(getColor(R.color.gray))

        weightInput.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val isNotEmpty = !p0.isNullOrEmpty()
                inputLayout.isHelperTextEnabled = false
                nextButton.isEnabled = isNotEmpty && unitSelected
                nextButton.setBackgroundColor(if (nextButton.isEnabled) getColor(R.color.calm) else getColor(R.color.gray))
                nextButton.alpha = if (nextButton.isEnabled) 1f else 0.5f
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

        backButton.setOnClickListener {
            dialog.dismiss()
        }

        nextButton.setOnClickListener {
            val weightText = weightInput.text.toString().trim()
            if (weightText.isNotEmpty()) {
                try {
                    val weight = weightText.toDouble() // Convert input to Double
                    val intent = Intent(this@MainActivity, ExerciseActivity::class.java)
                    intent.putExtra("DUMBBELL_WEIGHT", weight) // Pass the weight as Double
                    intent.putExtra("UNIT", unit)
                    startActivity(intent)
                    dialog.dismiss()
                    // Hide the soft keyboard

//                    ttsHelper.speakText("Starting in five seconds")


//                    val intent = Intent(this@MainActivity, ExerciseActivity::class.java)
//                    intent.putExtra("DUMBBELL_WEIGHT", weight) // Pass as Double
//                    startActivity(intent)
//                    dialog.dismiss()
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Invalid weight value", Toast.LENGTH_SHORT).show()
                }
            } else {
                weightInput.error = "Please Enter Weight"
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        ttsHelper.release()
    }

    fun Context.hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}