package com.example.liftapp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.liftapp.databinding.ActivitySignInBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        binding = ActivitySignInBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.currentUser?.let { user ->
            if (user.isEmailVerified) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                return
            }
        }

        binding.signIn.setOnClickListener {
            val email = binding.emailInput.editText?.text.toString().trim()
            val password = binding.passwordInput.editText?.text.toString().trim()

            if (validateInputs(email, password)) {
                signInUser(email, password)
            }
        }

        binding.forgetPassword.setOnClickListener {
            showForgotPasswordDialog()
        }


        binding.signUp.setOnClickListener {
            Toast.makeText(this, "You Clicked", Toast.LENGTH_SHORT).show()
            val toSignUpPage = Intent(this, SignUpActivity::class.java)
            startActivity(toSignUpPage)
            finish()
        }
    }
    private fun validateInputs(email: String, password: String): Boolean {
        // Clear previous errors
        binding.emailInput.error = null
        binding.passwordInput.error = null

        return when {
            email.isEmpty() -> {
                binding.emailInput.error = "Email required"
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.emailInput.error = "Invalid email format"
                false
            }
            password.isEmpty() -> {
                binding.passwordInput.error = "Password required"
                false
            }
            else -> true
        }
    }
    private fun signInUser(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    checkEmailVerification()
                } else {
                    handleSignInError(task.exception)
                }
            }
    }
    private fun checkEmailVerification() {
        val user = firebaseAuth.currentUser
        if (user?.isEmailVerified == true) {
            // Email is verified - proceed to main activity
            Toast.makeText(this, "Sign in successful!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            // Email not verified
            Toast.makeText(
                this,
                "Please verify your email first. Check your inbox.",
                Toast.LENGTH_LONG
            ).show()
            firebaseAuth.signOut() // Optional: Force user to sign in again after verification
        }
    }

    private fun handleSignInError(exception: Exception?) {
        val errorMessage = when (exception) {
            is FirebaseAuthInvalidUserException -> "Account not found. Please sign up first."
            is FirebaseAuthInvalidCredentialsException -> "Invalid credentials. Check email/password."
            else -> "Authentication failed: ${exception?.message}"
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun showForgotPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_forgot_password, null)
        val emailInput = dialogView.findViewById<TextInputEditText>(R.id.emailEditText)

        builder.setView(dialogView)
            .setTitle("Reset Password")
            .setPositiveButton("Submit") { dialog, _ ->
                val email = emailInput.text.toString().trim()
                if (validateForgotPasswordEmail(email)) {
                    sendPasswordResetEmail(email)
                }
                dialog.dismiss()
            }
//            .setNegativeButton("Cancel") { dialog, _ ->
//                dialog.dismiss()
//            }

        builder.create().show()
    }
    private fun validateForgotPasswordEmail(email: String): Boolean {
        binding.emailInput.error = null

        return when {
            email.isEmpty() -> {
                Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show()
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun sendPasswordResetEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Always show success message for security reasons
                    Toast.makeText(
                        this,
                        "If an account exists, a password reset email has been sent.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    // Handle specific errors
                    when (task.exception) {
                        is FirebaseAuthInvalidUserException -> {
                            Toast.makeText(
                                this,
                                "No account found with this email address",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else -> {
                            Toast.makeText(
                                this,
                                "Failed to send reset email: ${task.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
    }
}