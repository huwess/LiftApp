package com.example.liftapp.helper.calculator

import android.annotation.SuppressLint

class Calculator {

    // Function to convert kg to lb
    @SuppressLint("DefaultLocale")
    fun kgToLb(kg: Double): Double {
        return String.format("%.1f", kg * 2.20462).toDouble() // 1 kg = 2.20462 lbs, rounded to 1 decimal place
    }

    // Function to convert lb to kg
    @SuppressLint("DefaultLocale")
    fun lbToKg(lb: Double): Double {
        return String.format("%.1f", lb / 2.20462).toDouble() // 1 lb = 0.453592 kg, rounded to 1 decimal place
    }
}
