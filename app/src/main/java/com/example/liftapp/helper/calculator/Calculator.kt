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

    fun oneRepMaxCalculator(weightload: Double, reps: Int): Double {
        if (reps < 6){ // Epley's formula
            return weightload * (1 + (reps / 30))
        } else { // Brzycki's formula
            return weightload / (1.0278 - 0.0278 * reps)
        }
    }

    fun assStrengthLvl (age: Int, weight: Double, unit: Int, oneRepMax: Double, gender: String): String {
        val ageLvl = getAgeLvl(age)
        val weightLvl: Int

        if (gender == "Male") {
            if(unit == 1) {
                weightLvl = getWeightLvlMaleKg(weight)
                return getStrengthLvlMaleKg(ageLvl, weightLvl, oneRepMax)
            } else {
                weightLvl = getWeightLvlMaleLb(weight)
                return getStrengthLvlMaleLb(ageLvl, weightLvl, oneRepMax)
            }
        } else {
            if (unit == 1) {
                weightLvl = getWeightLvlFemaleKg(weight)
                return getStrengthLvlFemaleKg(ageLvl, weightLvl, oneRepMax)
            } else {
                weightLvl = getWeightLvlFemaleLb(weight)
                return getStrengthLvlFemaleLb(ageLvl, weightLvl, oneRepMax)
            }
        }
    }

    fun getAgeLvl (age: Int): Int {
        return when {
            age in 18..39 -> 1
            age in 40..49 -> 2
            else -> 1
        }
    }

    fun getWeightLvlMaleKg (weight: Double): Int {
        return when {
            weight in 52.0..55.9 -> 1
            weight in 56.0..59.9 -> 2
            weight in 60.0..66.9 -> 3
            weight in 67.0..74.9 -> 4
            weight in 75.0..81.9 -> 5
            weight in 82.0..89.9 -> 6
            weight in 90.0..99.9 -> 7
            weight in 100.0..109.9 -> 8
            weight in 110.0..124.9 -> 9
            weight in 125.0..144.9 -> 10
            weight == 145.0 -> 11
            weight > 145.0 -> 12
            else -> 1
        }
    }

    fun getWeightLvlMaleLb (weight: Double): Int {
        return when {
            weight in 114.0..122.9 -> 1
            weight in 123.0..131.9 -> 2
            weight in 132.0..147.9 -> 3
            weight in 148.0..164.9 -> 4
            weight in 165.0..180.9 -> 5
            weight in 181.0..197.9 -> 6
            weight in 198.0..219.9 -> 7
            weight in 220.0..241.9 -> 8
            weight in 242.0..274.9 -> 9
            weight in 275.0..318.9 -> 10
            weight in 319.0..319.9 -> 11
            weight > 320.0 -> 12
            else -> 1
        }
    }

    fun getStrengthLvlMaleKg (age: Int, weight: Int, oneRepMax: Double): String {
        val classLvl = getClassLvlMale(age, weight)

        val strengthLevels = mapOf(
            1 to listOf(32.4, 39.9, 49.9, 59.9),
            2 to listOf(34.9, 44.9, 52.4, 64.9),
            3 to listOf(37.4, 47.4, 77.4, 69.9),
            4 to listOf(42.4, 55.9, 61.4, 77.4),
            5 to listOf(44.9, 57.4, 69.9, 84.9),
            6 to listOf(49.9, 62.4, 75.9, 99.9),
            7 to listOf(52.4, 64.9, 77.4, 104.9),
            8 to listOf(54.9, 69.9, 82.4, 114.9),
            9 to listOf(57.4, 72.4, 84.9, 119.9),
            10 to listOf(59.9, 74.9, 87.4, 122.4),
            11 to listOf(59.9, 74.9, 89.9, 124.9),
            12 to listOf(62.4, 77.4, 92.4, 129.9)
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

    fun getStrengthLvlMaleLb (age: Int, weight: Int, oneRepMax: Double): String {
        val classLvl = getClassLvlMale(age, weight)

        val strengthLevels = mapOf(
            1 to listOf(74.9, 89.9, 109.9, 129.9),
            2 to listOf(79.9, 99.9, 114.9, 139.9),
            3 to listOf(84.9, 104.9, 124.9, 149.9),
            4 to listOf(94.9, 119.9, 139.9, 169.9),
            5 to listOf(99.9, 129.9, 154.9, 189.9),
            6 to listOf(109.9, 139.9, 164.9, 219.9),
            7 to listOf(114.9, 144.9, 174.9, 234.9),
            8 to listOf(119.9, 154.9, 184.9, 254.9),
            9 to listOf(124.9, 159.9, 189.9, 264.9),
            10 to listOf(129.9, 164.9, 194.9, 274.9),
            11 to listOf(134.9, 169.9, 199.9, 279.9),
            12 to listOf(139.9, 174.9, 204.9, 284.9)
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

    fun getClassLvlMale (age: Int, weight: Int): Int {
        return when (age) {
            1 -> weight // age 18-39
            2 -> weight + 12 //  age 40-49
            else -> 1
        }
    }

    fun getWeightLvlFemaleKg (weight: Double): Int {
        return when {
            weight in 44.0..47.9 -> 1
            weight in 48.0..51.9 -> 2
            weight in 52.0..55.9 -> 3
            weight in 56.0..59.9 -> 4
            weight in 60.0..66.9 -> 5
            weight in 67.0..74.9 -> 6
            weight in 75.0..81.9 -> 7
            weight in 82.0..89.9 -> 8
            weight == 90.0 -> 9
            weight > 90.0 -> 10
            else -> 1
        }
    }

    fun getWeightLvlFemaleLb (weight: Double): Int {
        return when {
            weight in 97.0..104.9 -> 1
            weight in 105.0..113.9 -> 2
            weight in 114.0..122.9 -> 3
            weight in 123.0..131.9 -> 4
            weight in 132.0..147.9 -> 5
            weight in 148.0..164.9 -> 6
            weight in 165.0..180.9 -> 7
            weight in 181.0..197.9 -> 8
            weight in 198.0..198.9 -> 9
            weight > 199.0 -> 10
            else -> 1
        }
    }

    fun getStrengthLvlFemaleKg (age: Int, weight: Int, oneRepMax: Double): String {
        val classLvl = getClassLvlFemale(age, weight)

        val strengthLevels = mapOf(
            1 to listOf(17.4, 22.4, 29.9, 39.9),
            2 to listOf(19.9, 24.9, 32.4, 42.4),
            3 to listOf(22.4, 27.4, 34.9, 44.9),
            4 to listOf(22.4, 27.4, 37.4, 47.4),
            5 to listOf(24.9, 29.9, 39.9, 49.9),
            6 to listOf(27.4, 32.4, 42.4, 54.9),
            7 to listOf(29.9, 34.9, 47.4, 62.4),
            8 to listOf(32.4, 37.4, 49.9, 64.9),
            9 to listOf(34.9, 39.9, 52.4, 67.4),
            10 to listOf(37.4, 42.4, 57.4, 72.4)
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

    fun getStrengthLvlFemaleLb (age: Int, weight: Int, oneRepMax: Double): String {
        val classLvl = getClassLvlFemale(age, weight)

        val strengthLevels = mapOf(
            1 to listOf(39.9, 49.9, 64.9, 84.9),
            2 to listOf(44.9, 54.9, 69.9, 89.9),
            3 to listOf(49.9, 59.9, 74.9, 99.9),
            4 to listOf(49.9, 59.9, 79.9, 104.9),
            5 to listOf(54.9, 64.9, 84.9, 109.9),
            6 to listOf(59.9, 69.9, 94.9, 119.9),
            7 to listOf(64.9, 74.9, 104.9, 134.9),
            8 to listOf(69.9, 79.9, 109.9, 139.9),
            9 to listOf(74.9, 84.9, 114.9, 149.9),
            10 to listOf(79.9, 94.9, 124.9, 159.9)
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

    fun getClassLvlFemale (age: Int, weight: Int): Int {
        return when (age) {
            1 -> weight // age 18-39
            2 -> weight + 10 //  age 40-49
            else -> 1
        }
    }

}
