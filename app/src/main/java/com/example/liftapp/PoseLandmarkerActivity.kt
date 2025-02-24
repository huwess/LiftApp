package com.example.liftapp

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.liftapp.databinding.ActivityPoseLandmarkerBinding

class PoseLandmarkerActivity : AppCompatActivity() {
    private lateinit var buttonMoveNet: Button

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityPoseLandmarkerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        binding = ActivityPoseLandmarkerBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        buttonMoveNet = findViewById(R.id.try_movenet_button)

//        setSupportActionBar(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        binding.navigation.setupWithNavController(navController)
        binding.navigation.setOnNavigationItemReselectedListener {
            // ignore the reselection
        }

        // Set an OnClickListener on the button
//        buttonMoveNet.setOnClickListener {
//            // Create an Intent to start Activity2
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}