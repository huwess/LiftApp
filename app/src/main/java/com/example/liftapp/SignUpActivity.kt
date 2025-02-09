package com.example.liftapp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.liftapp.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.signUp.setOnClickListener {
            val email = binding.emailInput.editText?.text.toString().trim()
            val password = binding.passwordInput.editText?.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and Password cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isEmailValid(email)) { // Check email format
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Proceed with Firebase signup
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        sendEmailVerification() // Send verification email
                    } else {
                        handleSignupError(task.exception) // Handle errors
                    }
                }
        }

        binding.signIn.setOnClickListener {
            val toSignInPage = Intent(this, SignInActivity::class.java)
            startActivity(toSignInPage)
            finish()
        }

    }

    private fun sendEmailVerification() {
        val user = firebaseAuth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Verification email sent. Check your inbox!",
                        Toast.LENGTH_LONG
                    ).show()
                    // Redirect to login or verification pending screen
                    startActivity(Intent(this, SignInActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Failed to send verification email: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    // Add this function to your SignUpActivity
    private fun isEmailValid(email: String): Boolean {
        val emailRegex = Patterns.EMAIL_ADDRESS
        return emailRegex.matcher(email).matches()
    }

    private fun handleSignupError(exception: Exception?) {
        when (exception) {
            is FirebaseAuthInvalidCredentialsException -> {
                Toast.makeText(
                    this,
                    "Invalid email format or password requirements not met",
                    Toast.LENGTH_SHORT
                ).show()
            }
            is FirebaseAuthUserCollisionException -> {
                Toast.makeText(
                    this,
                    "Email already in use. Try logging in instead.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                Toast.makeText(
                    this,
                    "Signup failed: ${exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}