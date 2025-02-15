package com.example.liftapp.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.liftapp.R
import com.example.liftapp.databinding.FragmentEssentialsBinding

class EssentialsFragment : Fragment() {


    private var _fragmentEssentialBinding: FragmentEssentialsBinding? = null
    private val fragmentEssentialsBinding get() = _fragmentEssentialBinding!!

    private var selectedGender: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentEssentialBinding = FragmentEssentialsBinding.inflate(inflater, container, false)
        return fragmentEssentialsBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentEssentialsBinding.maleOption.setOnClickListener{
            selectGender("Male")
        }
        fragmentEssentialsBinding.femaleOption.setOnClickListener {
            selectGender("Female")
        }
    }

    private fun selectGender(gender: String) {
        selectedGender = gender // Add this line
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

    fun getEnteredName(): String = fragmentEssentialsBinding.name.editText?.text.toString().trim()
    fun getEnteredAge(): Int = fragmentEssentialsBinding.age.editText?.text.toString().toInt()
    fun getEnteredWeight(): Int = fragmentEssentialsBinding.bodyWeight.editText?.text.toString().toInt()
    fun getSelectedUnit(): Int = if (fragmentEssentialsBinding.unit.isChecked) 1 else 0
    fun getSelectedGender(): String? = selectedGender

}