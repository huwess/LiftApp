package com.example.liftapp.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import com.example.liftapp.R
import com.example.liftapp.databinding.FragmentEssentialsBinding
import com.example.liftapp.helper.users.UserViewModel
import androidx.fragment.app.activityViewModels

class EssentialsFragment : Fragment() {


    private var _fragmentEssentialBinding: FragmentEssentialsBinding? = null
    private val fragmentEssentialsBinding get() = _fragmentEssentialBinding!!
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentEssentialBinding = FragmentEssentialsBinding.inflate(inflater, container, false)
        return fragmentEssentialsBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentEssentialsBinding.name.editText?.doAfterTextChanged { text ->
            userViewModel.setName(text?.toString()?.trim() ?: "")
        }

        fragmentEssentialsBinding.age.editText?.doAfterTextChanged { text ->
            userViewModel.setAge(text?.toString()?.toIntOrNull() ?: 0)
        }

        fragmentEssentialsBinding.bodyWeight.editText?.doAfterTextChanged { text ->
            userViewModel.setWeight(text.toString().toDouble())
        }

        fragmentEssentialsBinding.maleOption.setOnClickListener { selectGender("Male") }
        fragmentEssentialsBinding.femaleOption.setOnClickListener { selectGender("Female") }

        fragmentEssentialsBinding.unit.setOnCheckedChangeListener { _, isChecked ->
            userViewModel.setUnit(if (isChecked) 1 else 0)
        }



    }


    private fun selectGender(gender: String) {
        userViewModel.setGender(gender)
        when (gender) {
            "Male" -> {
                fragmentEssentialsBinding.maleRadioButton.isChecked = true
                fragmentEssentialsBinding.femaleRadioButton.isChecked = false

                // Change background color to indicate selection
                fragmentEssentialsBinding.maleOption.setBackgroundResource(R.drawable.gender_rectangle_selected)
                fragmentEssentialsBinding.femaleOption.setBackgroundResource(R.drawable.gender_rectangle)
            }
            "Female" -> {
                fragmentEssentialsBinding.maleRadioButton.isChecked = false
                fragmentEssentialsBinding.femaleRadioButton.isChecked = true

                // Change background color to indicate selection
                fragmentEssentialsBinding.femaleOption.setBackgroundResource(R.drawable.gender_rectangle_selected)
                fragmentEssentialsBinding.maleOption.setBackgroundResource(R.drawable.gender_rectangle)
            }
        }

    }


}