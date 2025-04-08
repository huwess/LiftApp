package com.bigbadbooks.liftapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bigbadbooks.liftapp.databinding.ActivityMainBinding
import com.bigbadbooks.liftapp.bottom_nav.HomeFragment
import com.bigbadbooks.liftapp.bottom_nav.SettingsFragment
import com.bigbadbooks.liftapp.bottom_nav.fab_add.ExerciseActivity
import com.bigbadbooks.liftapp.helper.audio.TextToSpeechHelper
import com.google.firebase.auth.FirebaseAuth
import com.bigbadbooks.liftapp.helper.users.UserProfileHelper
import com.bigbadbooks.liftapp.helper.users.UserViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    var isHomeDataLoaded = false

    private lateinit var prefManager: PrefMnager

    private lateinit var userProfileHelper: UserProfileHelper
    private lateinit var ttsHelper: TextToSpeechHelper
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { !isHomeDataLoaded }
        super.onCreate(savedInstanceState)
//        TimeoutHandler.startTimeout(this)
//        checkInternetConnection()
        binding = ActivityMainBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        Log.d("USERLOG", currentUser?.email.toString())
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


    private fun replaceFragment(fragment: Fragment) {
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

        Glide.with(mview).asGif().load(R.drawable.press).into(imageView)

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

    fun onHomeDataLoaded() {
        isHomeDataLoaded = true
    }

    private fun checkInternetConnection() {
        val connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isConnected = snapshot.getValue(Boolean::class.java) ?: false
                if (!isConnected) {
                    Toast.makeText(this@MainActivity, "You're offline. Data will sync when online.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "Error checking connection: ${error.message}")
            }
        })



    }



}

object TimeoutHandler {
    fun startTimeout(activity: MainActivity) {
        object : CountDownTimer(2000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                if (!activity.isHomeDataLoaded) {
                    activity.onHomeDataLoaded()
                    Log.d("TimeoutHandler", "Timeout reached; dismissing splash screen.")
                }
            }
        }.start()
    }
}
