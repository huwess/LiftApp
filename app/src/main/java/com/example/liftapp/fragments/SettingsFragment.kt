package com.example.liftapp.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.liftapp.R
import com.example.liftapp.SignInActivity
import com.example.liftapp.databinding.FragmentSettingsBinding
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private var _fragmentSettingBinding: FragmentSettingsBinding? = null
    private val fragmentSettingsBinding get() = _fragmentSettingBinding!!



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