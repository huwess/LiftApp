package com.example.liftapp.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.liftapp.R
import com.example.liftapp.SignInActivity
import com.example.liftapp.databinding.FragmentSettingsBinding
import com.example.liftapp.helper.users.UserViewModel
import com.google.firebase.auth.FirebaseAuth

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
                R.id.signOut -> signOutUser()
            }
            true
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
}