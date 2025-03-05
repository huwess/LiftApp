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
    @SuppressLint("DefaultLocale")
    fun getPercentageWeight(oneRepMax: Double, percent : Double) : Double {
        return oneRepMax * percent
    }

    fun oneRepMaxCalculator(weightload: Double, reps: Int): Double {
        val result: Double

        if (reps < 6){ // Epley's formula
            result = weightload * (1 + (reps / 30))
            return result
        } else { // Brzycki's formula
            result = weightload / (1.0278 - 0.0278 * reps)
            return result
        }
    }

    fun calculateRepetitions(oneRepMax: Double, weightLifted: Double, repetition: Int): Int {
        require(oneRepMax > 0) { "One-rep max must be greater than zero." }
        require(weightLifted > 0) { "Weight lifted must be greater than zero." }
        require(weightLifted < oneRepMax) { "Weight lifted must be less than one-rep max." }

        val result : Int

        if(repetition < 6) {
            // Epley Formula: 1RM = weight * (1 + reps / 30)
            // Rearranged to solve for reps: reps = 30 * (1RM / weight - 1)
            result = (30 * (oneRepMax / weightLifted - 1)).toInt()
        } else {
            // Brzycki Formula: 1RM = weight * 36 / (37 - reps)
            // Rearranged to solve for reps: reps = 37 - (36 * weight / 1RM)
            result = (37 - (36 * weightLifted / oneRepMax)).toInt()
        }

        return result
    }

    fun assStrengthLvl (age: Int, weight: Double, oneRepMax: Double, gender: String, weightUnit: Int, oneRepMaxUnit: Int): String {
        val ageLvl = getAgeLvl(age)
        val adjustedWeight: Double
        val adjustedOneRepMax: Double

        if (weightUnit == 1) adjustedWeight = weight / 2.205 else adjustedWeight = weight
        if (oneRepMaxUnit == 1) adjustedOneRepMax = oneRepMax / 2.205 else adjustedOneRepMax = oneRepMax

        val weightLvl = getWeightLvl(adjustedWeight)

        if (gender == "Male") {
            return getStrengthLvlMaleKg(ageLvl, weightLvl, adjustedOneRepMax)
        } else {
            return getStrengthLvlFemaleKg(ageLvl, weightLvl, adjustedOneRepMax)
        }
    }

    fun getAgeLvl (age: Int): Int {
        return when {
            age in 15..19 -> 1
            age in 20..29 -> 2
            age in 30..39 -> 3
            age in 40..49 -> 4
            else -> 1
        }
    }

    fun getWeightLvl (weight: Double): Int {
        return when {
            weight in 57.0..67.9 -> 1
            weight in 68.0..78.9 -> 2
            weight in 79.0..90.9 -> 3
            weight in 91.0..101.9 -> 4
            weight in 102.0..112.9 -> 5
            weight in 113.0..135.9 -> 6
            weight > 136.0 -> 7
            else -> 1
        }
    }

    fun getStrengthLvlMaleKg (age: Int, weight: Int, oneRepMax: Double): String {
        val classLvl = getClassLvl(age, weight)

        val strengthLevels = mapOf(
            // age 15 - 19
            1 to listOf(25.9, 32.9, 40.9, 52.9),
            2 to listOf(30.9, 39.9, 48.9, 63.9),
            3 to listOf(33.9, 43.9, 53.9, 69.9),
            4 to listOf(38.9, 49.9, 60.9, 80.9),
            5 to listOf(39.9, 50.9, 62.9, 82.9),
            6 to listOf(40.9, 52.9, 64.9, 84.9),
            7 to listOf(42.9, 55.9, 68.9, 89.9),
            // age 20 - 29
            8 to listOf(31.9, 41.9, 50.9, 66.9),
            9 to listOf(38.9, 50.9, 61.9, 81.9),
            10 to listOf(42.9, 54.9, 67.9, 88.9),
            11 to listOf(48.9, 62.9, 77.9, 101.9),
            12 to listOf(49.9, 64.9, 79.9, 104.9),
            13 to listOf(51.9, 66.9, 81.9, 107.9),
            14 to listOf(54.9, 69.9, 86.9, 113.9),
            // age 30 - 39
            15 to listOf(34.9, 44.9, 54.9, 71.9),
            16 to listOf(41.9, 53.9, 66.9, 87.9),
            17 to listOf(45.9, 58.9, 72.9, 95.9),
            18 to listOf(52.9, 67.9, 83.9, 108.9),
            19 to listOf(53.9, 69.9, 85.9, 111.9),
            20 to listOf(55.9, 71.9, 88.9, 115.9),
            21 to listOf(58.9, 75.9, 92.9, 121.9),
            // age 40 - 49
            22 to listOf(29.9, 37.9, 46.9, 61.9),
            23 to listOf(35.9, 46.9, 57.9, 74.9),
            24 to listOf(39.9, 50.9, 62.9, 81.9),
            25 to listOf(44.9, 57.9, 71.9, 93.9),
            26 to listOf(46.9, 59.9, 73.9, 96.9),
            27 to listOf(47.9, 61.9, 75.9, 99.9),
            28 to listOf(50.9, 64.9, 79.9, 104.9)
        )

        val thresholds = strengthLevels[classLvl] ?: return "Unknown"

        return when {
            oneRepMax <= thresholds[0] -> "Untrained"
            oneRepMax <= thresholds[1] -> "Novice"
            oneRepMax <= thresholds[2] -> "Intermediate"
            oneRepMax <= thresholds[3] -> "Advanced"
            else -> "Elite"
        }
    }

    fun getStrengthLvlFemaleKg (age: Int, weight: Int, oneRepMax: Double): String {
        val classLvl = getClassLvl(age, weight)

        val strengthLevels = mapOf(
            //age 15 - 19
            1 to listOf(13.9, 17.9, 22.9, 30.9),
            2 to listOf(16.9, 19.9, 26.9, 36.9),
            3 to listOf(17.9, 21.9, 28.9, 39.9),
            4 to listOf(20.9, 23.9, 31.9, 42.9),
            5 to listOf(21.9, 25.9, 34.9, 45.9),
            6 to listOf(23.9, 27.9, 36.9, 47.9),
            7 to listOf(26.9, 30.9, 40.9, 51.9),
            //age 20 - 29
            1 to listOf(17.9, 21.9, 28.9, 38.9),
            2 to listOf(20.9, 25.9, 34.9, 46.9),
            3 to listOf(22.9, 27.9, 36.9, 49.9),
            4 to listOf(25.9, 30.9, 39.9, 53.9),
            5 to listOf(27.9, 32.9, 44.9, 58.9),
            6 to listOf(30.9, 34.9, 46.9, 59.9),
            7 to listOf(33.9, 38.9, 51.9, 67.9),
            //age 30 - 39
            1 to listOf(18.9, 23.9, 31.9, 41.9),
            2 to listOf(22.9, 27.9, 36.9, 49.9),
            3 to listOf(24.9, 29.9, 39.9, 53.9),
            4 to listOf(27.9, 32.9, 42.9, 57.9),
            5 to listOf(29.9, 34.9, 47.9, 62.9),
            6 to listOf(32.9, 37.9, 49.9, 64.9),
            7 to listOf(36.9, 41.9, 55.9, 70.9),
            //age 40 - 49
            1 to listOf(15.9, 20.9, 26.9, 35.9),
            2 to listOf(18.9, 23.9, 30.9, 42.9),
            3 to listOf(20.9, 25.9, 33.9, 46.9),
            4 to listOf(23.9, 27.9, 36.9, 49.9),
            5 to listOf(25.9, 29.9, 40.9, 53.9),
            6 to listOf(27.9, 32.9, 42.9, 55.9),
            7 to listOf(30.9, 35.9, 47.9, 60.9)
        )

        val thresholds = strengthLevels[classLvl] ?: return "Unknown"

        return when {
            oneRepMax <= thresholds[0] -> "Untrained"
            oneRepMax <= thresholds[1] -> "Novice"
            oneRepMax <= thresholds[2] -> "Intermediate"
            oneRepMax <= thresholds[3] -> "Advanced"
            else -> "Elite"
        }
    }

    fun getClassLvl (age: Int, weight: Int): Int {
        return when (age) {
            1 -> weight // age 15-19
            2 -> weight + 7//  age 20-29
            3 -> weight + 14//  age 30-39
            4 -> weight + 21//  age 40-49
            else -> 1
        }
    }

}
