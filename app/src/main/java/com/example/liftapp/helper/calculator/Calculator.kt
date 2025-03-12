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

    fun assStrengthLvl (age: Int, weight: Double, oneRepMax: Double, gender: String, weightUnit: Int, oneRepMaxUnit: Int, exerType: Int): String {
        val ageLvl = getAgeLvl(age)
        val adjustedWeight: Double
        val adjustedOneRepMax: Double

        if (weightUnit == 1) adjustedWeight = weight / 2.205 else adjustedWeight = weight
        if (oneRepMaxUnit == 1) adjustedOneRepMax = oneRepMax / 2.205 else adjustedOneRepMax = oneRepMax

        val weightLvl = getWeightLvl(adjustedWeight)

        if(exerType == 1){ // Bench Press
            return when {
                gender == "Male" -> getStrBenchPressLvlMaleKg(ageLvl, weightLvl, adjustedOneRepMax)
                else -> getStrBenchPressLvlFemaleKg(ageLvl, weightLvl, adjustedOneRepMax)
            }
        } else if (exerType == 2){ // Deadlift
            return when {
                gender == "Male" -> getStrDeadliftLvlMaleKg(ageLvl, weightLvl, adjustedOneRepMax)
                else -> getStrDeadliftLvlFemaleKg(ageLvl, weightLvl, adjustedOneRepMax)
            }
        }else{ // Press
            return when {
                gender == "Male" -> getStrPressLvlMaleKg(ageLvl, weightLvl, adjustedOneRepMax)
                else -> getStrPressLvlFemaleKg(ageLvl, weightLvl, adjustedOneRepMax)
            }
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

    fun getClassLvl (age: Int, weight: Int): Int {
        return when (age) {
            1 -> weight // age 15-19
            2 -> weight + 7//  age 20-29
            3 -> weight + 14//  age 30-39
            4 -> weight + 21//  age 40-49
            else -> 1
        }
    }

    // Bench Press
    fun getStrBenchPressLvlMaleKg (age: Int, weight: Int, oneRepMax: Double): String {
        val classLvl = getClassLvl(age, weight)

        val strengthLevels = mapOf(
            // age 15 - 19
            1 to listOf(46.9, 60.9, 73.9, 84.9),
            2 to listOf(58.9, 74.9, 91.9, 106.9),
            3 to listOf(59.9, 76.9, 93.9, 109.9),
            4 to listOf(70.9, 90.9, 110.9, 131.9),
            5 to listOf(73.9, 94.9, 115.9, 133.9),
            6 to listOf(73.9, 95.9, 116.9, 136.9),
            7 to listOf(73.9, 94.9, 115.9, 137.9),
            // age 20 - 29
            8 to listOf(50.9, 64.9, 79.9, 91.9),
            9 to listOf(69.9, 89.9, 109.9, 129.9),
            10 to listOf(69.9, 89.9, 109.9, 149.9),
            11 to listOf(72.9, 92.9, 113.9, 160.9),
            12 to listOf(76.9, 98.9, 120.9, 169.9),
            13 to listOf(81.9, 105.9, 128.9, 180.9),
            14 to listOf(84.9, 108.9, 132.9, 181.9),
            // age 30 - 39
            15 to listOf(54.9, 70.9, 86.9, 113.9),
            16 to listOf(74.9, 95.9, 116.9, 154.9),
            17 to listOf(74.9, 96.9, 118.9, 155.9),
            18 to listOf(82.9, 106.9, 130.9, 172.9),
            19 to listOf(83.9, 107.9, 134.9, 175.9),
            20 to listOf(84.9, 109.9, 138.9, 176.9),
            21 to listOf(93.9, 120.9, 147.9, 194.9),
            // age 40 - 49
            22 to listOf(51.9, 65.9, 80.9, 104.9),
            23 to listOf(56.9, 72.9, 88.9, 115.9),
            24 to listOf(66.9, 85.9, 104.9, 134.9),
            25 to listOf(74.9, 96.9, 117.9, 142.9),
            26 to listOf(77.9, 99.9, 121.9, 153.9),
            27 to listOf(80.9, 103.9, 126.9, 163.9),
            28 to listOf(86.9, 110.9, 135.9, 165.9)
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

    fun getStrBenchPressLvlFemaleKg (age: Int, weight: Int, oneRepMax: Double): String {
        val classLvl = getClassLvl(age, weight)

        val strengthLevels = mapOf(
            //age 15 - 19
            1 to listOf(18.9, 26.9, 36.9, 50.9),
            2 to listOf(27.9, 38.9, 53.9, 74.9),
            3 to listOf(26.9, 36.9, 50.9, 69.9),
            4 to listOf(27.9, 38.9, 53.9, 74.9),
            5 to listOf(29.9, 40.9, 56.9, 77.9),
            6 to listOf(30.9, 41.9, 57.9, 80.9),
            7 to listOf(31.9, 43.9, 60.9, 83.9),
            //age 20 - 29
            8 to listOf(18.9, 25.9, 35.9, 48.9),
            9 to listOf(26.9, 36.9, 50.9, 69.9),
            10 to listOf(28.9, 39.9, 55.9, 76.9),
            11 to listOf(29.9, 41.9, 56.9, 78.9),
            12 to listOf(33.9, 46.9, 63.9, 88.9),
            13 to listOf(34.9, 47.9, 65.9, 90.9),
            14 to listOf(38.9, 53.9, 74.9, 102.9),
            //age 30 - 39
            15 to listOf(16.9, 21.9, 26.9, 56.9),
            16 to listOf(40.9, 52.9, 64.9, 82.9),
            17 to listOf(46.9, 59.9, 72.9, 88.9),
            18 to listOf(50.9, 65.9, 79.9, 97.9),
            19 to listOf(50.9, 66.9, 83.9, 99.9),
            20 to listOf(51.9, 65.9, 85.9, 101.9),
            21 to listOf(57.9, 73.9, 89.9, 105.9),
            //age 40 - 49
            22 to listOf(13.9, 24.9, 24.9, 45.9),
            23 to listOf(30.9, 35.9, 55.9, 65.9),
            24 to listOf(36.9, 40.9, 66.9, 74.9),
            25 to listOf(43.9, 43.9, 79.9, 80.9),
            26 to listOf(31.9, 31.9, 58.9, 84.9),
            27 to listOf(40.9, 40.9, 74.9, 86.9),
            28 to listOf(43.9, 43.9, 78.9, 92.9)
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

    // Deadlift

    fun getStrDeadliftLvlMaleKg (age: Int, weight: Int, oneRepMax: Double): String {
        val classLvl = getClassLvl(age, weight)

        val strengthLevels = mapOf(
            // age 15 - 19
            1 to listOf(73.9, 92.9, 102.9, 114.9),
            2 to listOf(96.9, 120.9, 133.9, 148.9),
            3 to listOf(121.9, 151.9, 168.9, 187.9),
            4 to listOf(128.9, 160.9, 178.9, 198.9),
            5 to listOf(134.9, 168.9, 187.9, 208.9),
            6 to listOf(136.9, 171.9, 190.9, 211.9),
            7 to listOf(138.9, 173.9, 193.9, 214.9),
            // age 20 - 29
            8 to listOf(80.9, 100.9, 126.9, 140.9),
            9 to listOf(111.9, 138.9, 185.9, 206.9),
            10 to listOf(130.9, 163.9, 218.9, 242.9),
            11 to listOf(143.9, 179.9, 239.9, 265.9),
            12 to listOf(145.9, 181.9, 242.9, 269.9),
            13 to listOf(146.9, 183.9, 244.9, 272.9),
            14 to listOf(148.9, 186.9, 248.9, 275.9),
            // age 30 - 39
            15 to listOf(88.9, 111.9, 148.9, 174.9),
            16 to listOf(111.9, 139.9, 185.9, 218.9),
            17 to listOf(131.9, 164.9, 219.9, 258.9),
            18 to listOf(141.9, 176.9, 235.9, 277.9),
            19 to listOf(144.9, 180.9, 241.9, 283.9),
            20 to listOf(145.9, 182.9, 243.9, 286.9),
            21 to listOf(153.9, 191.9, 256.9, 301.9),
            // age 40 - 49
            22 to listOf(79.9, 99.9, 117.9, 130.9),
            23 to listOf(91.9, 114.9, 153.9, 180.9),
            24 to listOf(108.9, 136.9, 181.9, 213.9),
            25 to listOf(112.9, 140.9, 187.9, 221.9),
            26 to listOf(123.9, 153.9, 205.9, 241.9),
            27 to listOf(129.9, 161.9, 216.9, 254.9),
            28 to listOf(132.9, 165.9, 220.9, 259.9)
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

    fun getStrDeadliftLvlFemaleKg (age: Int, weight: Int, oneRepMax: Double): String {
        val classLvl = getClassLvl(age, weight)

        val strengthLevels = mapOf(
            //age 15 - 19
            1 to listOf(38.9, 50.9, 61.9, 81.9),
            2 to listOf(50.9, 64.9, 79.9, 101.9),
            3 to listOf(55.9, 71.9, 87.9, 107.9),
            4 to listOf(58.9, 74.9, 91.9, 112.9),
            5 to listOf(60.9, 78.9, 98.9, 117.9),
            6 to listOf(61.9, 79.9, 102.9, 122.9),
            7 to listOf(68.9, 88.9, 108.9, 128.9),
            //age 20 - 29
            8 to listOf(57.9, 73.9, 90.9, 119.9),
            9 to listOf(69.9, 89.9, 109.9, 139.9),
            10 to listOf(76.9, 98.9, 120.9, 149.9),
            11 to listOf(78.9, 100.9, 123.9, 153.9),
            12 to listOf(81.9, 106.9, 132.9, 161.9),
            13 to listOf(81.9, 105.9, 133.9, 163.9),
            14 to listOf(83.9, 107.9, 131.9, 158.9),
            //age 30 - 39
            15 to listOf(69.9, 89.9, 109.9, 144.9),
            16 to listOf(73.9, 95.9, 116.9, 148.9),
            17 to listOf(80.9, 103.9, 126.9, 156.9),
            18 to listOf(82.9, 106.9, 130.9, 161.9),
            19 to listOf(90.9, 117.9, 146.9, 178.9),
            20 to listOf(92.9, 118.9, 150.9, 184.9),
            21 to listOf(93.9, 119.9, 146.9, 177.9),
            //age 40 - 49
            22 to listOf(53.9, 69.9, 84.9, 111.9),
            23 to listOf(66.9, 85.9, 104.9, 132.9),
            24 to listOf(68.9, 88.9, 108.9, 133.9),
            25 to listOf(73.9, 94.9, 115.9, 142.9),
            26 to listOf(77.9, 100.9, 125.9, 152.9),
            27 to listOf(80.9, 103.9, 131.9, 159.9),
            28 to listOf(81.9, 105.9, 128.9, 155.9)
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

    // Press

    fun getStrPressLvlMaleKg (age: Int, weight: Int, oneRepMax: Double): String {
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

    fun getStrPressLvlFemaleKg (age: Int, weight: Int, oneRepMax: Double): String {
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
            8 to listOf(17.9, 21.9, 28.9, 38.9),
            9 to listOf(20.9, 25.9, 34.9, 46.9),
            10 to listOf(22.9, 27.9, 36.9, 49.9),
            11 to listOf(25.9, 30.9, 39.9, 53.9),
            12 to listOf(27.9, 32.9, 44.9, 58.9),
            13 to listOf(30.9, 34.9, 46.9, 59.9),
            14 to listOf(33.9, 38.9, 51.9, 67.9),
            //age 30 - 39
            15 to listOf(18.9, 23.9, 31.9, 41.9),
            16 to listOf(22.9, 27.9, 36.9, 49.9),
            17 to listOf(24.9, 29.9, 39.9, 53.9),
            18 to listOf(27.9, 32.9, 42.9, 57.9),
            19 to listOf(29.9, 34.9, 47.9, 62.9),
            20 to listOf(32.9, 37.9, 49.9, 64.9),
            21 to listOf(36.9, 41.9, 55.9, 70.9),
            //age 40 - 49
            22 to listOf(15.9, 20.9, 26.9, 35.9),
            23 to listOf(18.9, 23.9, 30.9, 42.9),
            24 to listOf(20.9, 25.9, 33.9, 46.9),
            25 to listOf(23.9, 27.9, 36.9, 49.9),
            26 to listOf(25.9, 29.9, 40.9, 53.9),
            27 to listOf(27.9, 32.9, 42.9, 55.9),
            28 to listOf(30.9, 35.9, 47.9, 60.9)
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

}
