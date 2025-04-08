package com.bigbadbooks.liftapp.bottom_nav

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.bigbadbooks.liftapp.R
import com.bigbadbooks.liftapp.SignInActivity
import com.bigbadbooks.liftapp.bottom_nav.settings_menu.EditProfileActivity
import com.bigbadbooks.liftapp.databinding.ContactSupportDialogBinding
import com.bigbadbooks.liftapp.databinding.FeatureComingSoonBinding
import com.bigbadbooks.liftapp.databinding.FragmentSettingsBinding
import com.bigbadbooks.liftapp.helper.calculator.Calculator
import com.bigbadbooks.liftapp.helper.users.UserProfileHelper
import com.bigbadbooks.liftapp.helper.users.UserViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class SettingsFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private var _fragmentSettingBinding: FragmentSettingsBinding? = null
    private val fragmentSettingsBinding get() = _fragmentSettingBinding!!

    private val userViewModel: UserViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragmentSettingBinding = FragmentSettingsBinding.inflate(inflater,container, false)
        // Inflate the layout for this fragment
        return fragmentSettingsBinding.root
    }

    companion object {
        private const val TAG = "Settings"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()

        fragmentSettingsBinding.profile.text = userViewModel.name.value?.firstOrNull()?.uppercase()

        userViewModel.name.observe(viewLifecycleOwner) { name ->
            fragmentSettingsBinding.profile.text = name?.firstOrNull()?.uppercase() ?: "?"
            fragmentSettingsBinding.nameTextView.text = name ?: "Unknown"
        }

        userViewModel.email.observe(viewLifecycleOwner) { email ->
            fragmentSettingsBinding.emailTextView.text = email ?: "No Email"
        }

        userViewModel.weight.observe(viewLifecycleOwner) { weight ->
            val unitText = if (userViewModel.unit.value == 0) "kg" else "lb"
            fragmentSettingsBinding.weightTextView.text = "$weight$unitText"
        }

        userViewModel.age.observe(viewLifecycleOwner) { age ->
            fragmentSettingsBinding.ageTextView.text = age?.toString() ?: "N/A"
        }

        userViewModel.gender.observe(viewLifecycleOwner) { gender ->
            fragmentSettingsBinding.genderTextView.text = gender ?: "Not Specified"
        }
        fragmentSettingsBinding.navigationView.setNavigationItemSelectedListener{ menuItem ->
            when(menuItem.itemId) {
                R.id.editProfile -> {
                    openEditProfile()
                    true
                }
                R.id.editUnits -> {
                    firebaseAuth.currentUser?.let { openEditUnitDialog(it.uid) }
                    true
                }
                R.id.change_password -> {
                    showChangePasswordDialog()
                    true
                }
                R.id.signOut -> {
                    signOutUser()
                    true
                }
                R.id.appearance -> {
                    showComingSoon("Appearance")
                    true
                }
                R.id.privacy_center -> {
                    showComingSoon("Privacy Center")
                    true
                }
                R.id.about_us -> {
                    showAboutUsDialog()
                    true
                }
                R.id.contact_support -> {
                    showContactSupportDialog()
                    true
                }
                R.id.feedback -> {
                    showComingSoon("Feedback")
                    true
                }
                R.id.share_with_friends -> {
                    showShareSheet()
                    true
                }
                else -> false
            }

        }

    }

    private fun showShareSheet() {
        val shareMessage = "Check out this amazing app: https://play.google.com"
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareMessage)
        }

        // Launch the default Android share sheet
        startActivity(
            Intent.createChooser(shareIntent, "Share via")
        )
    }



    private fun showComingSoon(title: String) {
        val builder = AlertDialog.Builder(requireContext(), R.style.fullscreenalert)
        val featureBinding = FeatureComingSoonBinding.inflate(layoutInflater)
        builder.setView(featureBinding.root)
        val dialog = builder.create()
        dialog.show()

        dialog.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            // Optional: Remove any background that might limit size
            setBackgroundDrawableResource(android.R.color.white)
        }
        featureBinding.title.text = title

        featureBinding.backButton.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun showContactSupportDialog() {
        val builder = AlertDialog.Builder(requireContext(), R.style.fullscreenalert)
        val contact_supp_binding = ContactSupportDialogBinding.inflate(layoutInflater)
        builder.setView(contact_supp_binding.root)
        val dialog = builder.create()
        dialog.show()

        dialog.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            // Optional: Remove any background that might limit size
            setBackgroundDrawableResource(android.R.color.white)
        }



        contact_supp_binding.sendButton.setOnClickListener {
            val subjectText = contact_supp_binding.subject.editText?.text.toString()
            val contentText = contact_supp_binding.content.editText?.text.toString()

            if (subjectText.isEmpty()) {
                contact_supp_binding.subject.error = null
                contact_supp_binding.subject.error = "Subject Required"
            }
            if(contentText.isEmpty()) {
                contact_supp_binding.content.error = null
                contact_supp_binding.content.error = "Content Required"
            }
            if(subjectText.isNotEmpty() && contentText.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("garciajoshuajadge+lift@gmail.com","kyleanoracanonigo+lift@gmail.com", "villariaselroi+lift@gmail.com"))
                    putExtra(Intent.EXTRA_SUBJECT, subjectText)
                    putExtra(Intent.EXTRA_TEXT, contentText)
                }
                try {
                    startActivity(Intent.createChooser(intent, "Send Email"))
                } catch (ex: android.content.ActivityNotFoundException) {
                    // Handle the case where no email client is available
                    Toast.makeText(requireContext(), "No email client installed", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }

        }
        contact_supp_binding.backButton.setOnClickListener {
            dialog.dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshUserData()
    }
    private fun refreshUserData() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            // Fetch latest user data and update ViewModel
            userViewModel.fetchUserData(currentUser.uid)
        }
    }
    private fun signOutUser() {
        firebaseAuth.signOut()
        startActivity(Intent(requireActivity(), SignInActivity::class.java))
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragmentSettingBinding = null
    }
    private fun openEditProfile() {
        startActivity(Intent(requireContext(), EditProfileActivity::class.java))
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun openEditUnitDialog(userId: String) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.change_unit_dialog)
        dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val drawable: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.dialog_bg)
        dialog.window?.setBackgroundDrawable(drawable)
        dialog.setCancelable(false)

        val cancel = dialog.findViewById<MaterialButton>(R.id.edit_unit_cancel)
        val save = dialog.findViewById<MaterialButton>(R.id.edit_unit_save)
        val calc = Calculator()
        val switch = dialog.findViewById<Switch>(R.id.unit)

        if(userViewModel.unit.value == 0) {
            switch.isChecked = false
        } else {
            switch.isChecked = true
        }
        dialog.show()
        cancel.setOnClickListener{
            dialog.dismiss()
        }
        save.setOnClickListener{
            val userProfileHelper = UserProfileHelper()
            val weightValue = userViewModel.weight.value ?: 0.0  // Get the actual Double value safely
            var convertedWeight = 0.0
            if(switch.isChecked) {
                if(userViewModel.unit.value == 0) {
                    userViewModel.setUnit(1)
                    convertedWeight = calc.kgToLb(weightValue)
                    userProfileHelper.updateUserWeightAndUnit(userId, convertedWeight, 1) { success, error ->
                        if (success) {
                            Toast.makeText(requireContext(), "Unit updated!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Edit Unit failed",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                    dialog.dismiss()
                } else {
                    dialog.dismiss()
                }
            } else {
                if(userViewModel.unit.value == 1) {
                    userViewModel.setUnit(0)
                    convertedWeight = calc.lbToKg(weightValue)
                    userProfileHelper.updateUserWeightAndUnit(userId, convertedWeight, 0) { success, error ->
                        if (success) {
                            Toast.makeText(requireContext(), "Unit updated!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Edit Unit failed",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                    dialog.dismiss()
                } else {
                    dialog.dismiss()
                }
            }
            refreshUserData()
        }
    }

    private fun showAboutUsDialog() {
        val builder = AlertDialog.Builder(requireContext(), R.style.fullscreenalert)
        val mview = layoutInflater.inflate(R.layout.about_us_dialog, null)
        builder.setView(mview)
        val dialog = builder.create()
        dialog.show()

        dialog.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            // Optional: Remove any background that might limit size
            setBackgroundDrawableResource(android.R.color.white)
        }
        val backButton = mview.findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun showChangePasswordDialog() {
        val builder = AlertDialog.Builder(requireContext())
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

        return when {
            email.isEmpty() -> {
                Toast.makeText(requireContext(), "Email cannot be empty", Toast.LENGTH_SHORT).show()
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                Toast.makeText(requireContext(), "Invalid email format", Toast.LENGTH_SHORT).show()
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
                        requireContext(),
                        "Password reset email sent.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    // Handle specific errors
                    when (task.exception) {
                        is FirebaseAuthInvalidUserException -> {
                            Toast.makeText(
                                requireContext(),
                                "Address not found.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else -> {
                            Toast.makeText(
                                requireContext(),
                                "Failed to send email",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
    }
}