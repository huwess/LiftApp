package com.bigbadbooks.liftapp.helper.calculator

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
            1 to listOf(33.9, 46.9, 60.9, 73.9, 84.9),
            2 to listOf(41.9, 58.9, 74.9, 91.9, 106.9),
            3 to listOf(42.9, 59.9, 76.9, 93.9, 109.9),
            4 to listOf(50.9, 70.9, 90.9, 110.9, 131.9),
            5 to listOf(52.9, 73.9, 94.9, 115.9, 133.9),
            6 to listOf(52.9, 73.9, 95.9, 116.9, 136.9),
            7 to listOf(52.9, 73.9, 94.9, 115.9, 137.9),
            // age 20 - 29
            8 to listOf(35.9, 50.9, 64.9, 79.9, 91.9),
            9 to listOf(49.9, 69.9, 89.9, 109.9, 129.9),
            10 to listOf(49.9, 69.9, 89.9, 109.9, 149.9),
            11 to listOf(51.9, 72.9, 92.9, 113.9, 160.9),
            12 to listOf(54.9, 76.9, 98.9, 120.9, 169.9),
            13 to listOf(58.9, 81.9, 105.9, 128.9, 180.9),
            14 to listOf(60.9, 84.9, 108.9, 132.9, 181.9),
            // age 30 - 39
            15 to listOf(38.9, 54.9, 70.9, 86.9, 113.9),
            16 to listOf(52.9, 74.9, 95.9, 116.9, 154.9),
            17 to listOf(53.9, 74.9, 96.9, 118.9, 155.9),
            18 to listOf(58.9, 82.9, 106.9, 130.9, 172.9),
            19 to listOf(60.9, 83.9, 107.9, 134.9, 175.9),
            20 to listOf(62.9, 84.9, 109.9, 138.9, 176.9),
            21 to listOf(66.9, 93.9, 120.9, 147.9, 194.9),
            // age 40 - 49
            22 to listOf(36.9, 51.9, 65.9, 80.9, 104.9),
            23 to listOf(40.9, 56.9, 72.9, 88.9, 115.9),
            24 to listOf(47.9, 66.9, 85.9, 104.9, 134.9),
            25 to listOf(53.9, 74.9, 96.9, 117.9, 142.9),
            26 to listOf(55.9, 77.9, 99.9, 121.9, 153.9),
            27 to listOf(57.9, 80.9, 103.9, 126.9, 163.9),
            28 to listOf(61.9, 86.9, 110.9, 135.9, 165.9)
        )

        val thresholds = strengthLevels[classLvl] ?: return "Unknown"

        return when {
            oneRepMax <= thresholds[0] -> "Inactive"
            oneRepMax <= thresholds[1] -> "Physically Active"
            oneRepMax <= thresholds[2] -> "Beginner"
            oneRepMax <= thresholds[3] -> "Intermediate"
            oneRepMax <= thresholds[4] -> "Advanced"
            else -> "Elite"
        }
    }

    fun getStrBenchPressLvlFemaleKg (age: Int, weight: Int, oneRepMax: Double): String {
        val classLvl = getClassLvl(age, weight)

        val strengthLevels = mapOf(
            //age 15 - 19
            1 to listOf(13.9, 18.9, 26.9, 36.9, 50.9),
            2 to listOf(20.9, 27.9, 38.9, 53.9, 74.9),
            3 to listOf(18.9, 26.9, 36.9, 50.9, 69.9),
            4 to listOf(20.9, 27.9, 38.9, 53.9, 74.9),
            5 to listOf(21.9, 29.9, 40.9, 56.9, 77.9),
            6 to listOf(21.9, 30.9, 41.9, 57.9, 80.9),
            7 to listOf(22.9, 31.9, 43.9, 60.9, 83.9),
            //age 20 - 29
            8 to listOf(13.9, 18.9, 25.9, 35.9, 48.9),
            9 to listOf(18.9, 26.9, 36.9, 50.9, 69.9),
            10 to listOf(20.9, 28.9, 39.9, 55.9, 76.9),
            11 to listOf(21.9, 29.9, 41.9, 56.9, 78.9),
            12 to listOf(24.9, 33.9, 46.9, 63.9, 88.9),
            13 to listOf(24.9, 34.9, 47.9, 65.9, 90.9),
            14 to listOf(27.9, 38.9, 53.9, 74.9, 102.9),
            //age 30 - 39
            15 to listOf(11.9, 16.9, 21.9, 26.9, 56.9),
            16 to listOf(29.9, 40.9, 52.9, 64.9, 82.9),
            17 to listOf(32.9, 46.9, 59.9, 72.9, 88.9),
            18 to listOf(34.9, 50.9, 65.9, 79.9, 97.9),
            19 to listOf(37.9, 50.9, 66.9, 83.9, 99.9),
            20 to listOf(38.9, 51.9, 65.9, 85.9, 101.9),
            21 to listOf(40.9, 57.9, 73.9, 89.9, 105.9),
            //age 40 - 49
            22 to listOf(12.9, 13.9, 24.9, 24.9, 45.9),
            23 to listOf(19.9, 30.9, 35.9, 55.9, 65.9),
            24 to listOf(22.9, 36.9, 40.9, 66.9, 74.9),
            25 to listOf(23.9, 43.9, 43.9, 79.9, 80.9),
            26 to listOf(25.9, 31.9, 31.9, 58.9, 84.9),
            27 to listOf(25.9, 40.9, 40.9, 74.9, 86.9),
            28 to listOf(27.9, 43.9, 43.9, 78.9, 92.9)
        )

        val thresholds = strengthLevels[classLvl] ?: return "Unknown"

        return when {
            oneRepMax <= thresholds[0] -> "Inactive"
            oneRepMax <= thresholds[1] -> "Physically Active"
            oneRepMax <= thresholds[2] -> "Beginner"
            oneRepMax <= thresholds[3] -> "Intermediate"
            oneRepMax <= thresholds[4] -> "Advanced"
            else -> "Elite"
        }
    }

    // Deadlift

    fun getStrDeadliftLvlMaleKg (age: Int, weight: Int, oneRepMax: Double): String {
        val classLvl = getClassLvl(age, weight)

        val strengthLevels = mapOf(
            // age 15 - 19
            1 to listOf(44.9, 73.9, 92.9, 102.9, 114.9),
            2 to listOf(57.9, 96.9, 120.9, 133.9, 148.9),
            3 to listOf(72.9, 121.9, 151.9, 168.9, 187.9),
            4 to listOf(76.9, 128.9, 160.9, 178.9, 198.9),
            5 to listOf(80.9, 134.9, 168.9, 187.9, 208.9),
            6 to listOf(81.9, 136.9, 171.9, 190.9, 211.9),
            7 to listOf(83.9, 138.9, 173.9, 193.9, 214.9),
            // age 20 - 29
            8 to listOf(46.9, 80.9, 100.9, 126.9, 140.9),
            9 to listOf(63.9, 111.9, 138.9, 185.9, 206.9),
            10 to listOf(75.9, 130.9, 163.9, 218.9, 242.9),
            11 to listOf(82.9, 143.9, 179.9, 239.9, 265.9),
            12 to listOf(83.9, 145.9, 181.9, 242.9, 269.9),
            13 to listOf(84.9, 146.9, 183.9, 244.9, 272.9),
            14 to listOf(85.9, 148.9, 186.9, 248.9, 275.9),
            // age 30 - 39
            15 to listOf(50.9, 88.9, 111.9, 148.9, 174.9),
            16 to listOf(63.9, 111.9, 139.9, 185.9, 218.9),
            17 to listOf(75.9, 131.9, 164.9, 219.9, 258.9),
            18 to listOf(80.9, 141.9, 176.9, 235.9, 277.9),
            19 to listOf(82.9, 144.9, 180.9, 241.9, 283.9),
            20 to listOf(83.9, 145.9, 182.9, 243.9, 286.9),
            21 to listOf(88.9, 153.9, 191.9, 256.9, 301.9),
            // age 40 - 49
            22 to listOf(45.9, 79.9, 99.9, 117.9, 130.9),
            23 to listOf(52.9, 91.9, 114.9, 153.9, 180.9),
            24 to listOf(62.9, 108.9, 136.9, 181.9, 213.9),
            25 to listOf(64.9, 112.9, 140.9, 187.9, 221.9),
            26 to listOf(70.9, 123.9, 153.9, 205.9, 241.9),
            27 to listOf(74.9, 129.9, 161.9, 216.9, 254.9),
            28 to listOf(75.9, 132.9, 165.9, 220.9, 259.9)
        )

        val thresholds = strengthLevels[classLvl] ?: return "Unknown"

        return when {
            oneRepMax <= thresholds[0] -> "Inactive"
            oneRepMax <= thresholds[1] -> "Physically Active"
            oneRepMax <= thresholds[2] -> "Beginner"
            oneRepMax <= thresholds[3] -> "Intermediate"
            oneRepMax <= thresholds[4] -> "Advanced"
            else -> "Elite"
        }
    }

    fun getStrDeadliftLvlFemaleKg (age: Int, weight: Int, oneRepMax: Double): String {
        val classLvl = getClassLvl(age, weight)

        val strengthLevels = mapOf(
            //age 15 - 19
            1 to listOf(16.9, 38.9, 50.9, 61.9, 81.9),
            2 to listOf(21.9, 50.9, 64.9, 79.9, 101.9),
            3 to listOf(23.9, 55.9, 71.9, 87.9, 107.9),
            4 to listOf(24.9, 58.9, 74.9, 91.9, 112.9),
            5 to listOf(25.9, 60.9, 78.9, 98.9, 117.9),
            6 to listOf(26.9, 61.9, 79.9, 102.9, 122.9),
            7 to listOf(29.9, 68.9, 88.9, 108.9, 128.9),
            //age 20 - 29
            8 to listOf(24.9, 57.9, 73.9, 90.9, 119.9),
            9 to listOf(29.9, 69.9, 89.9, 109.9, 139.9),
            10 to listOf(32.9, 76.9, 98.9, 120.9, 149.9),
            11 to listOf(33.9, 78.9, 100.9, 123.9, 153.9),
            12 to listOf(34.9, 81.9, 106.9, 132.9, 161.9),
            13 to listOf(34.9, 81.9, 105.9, 133.9, 163.9),
            14 to listOf(35.9, 83.9, 107.9, 131.9, 158.9),
            //age 30 - 39
            15 to listOf(29.9, 69.9, 89.9, 109.9, 144.9),
            16 to listOf(31.9, 73.9, 95.9, 116.9, 148.9),
            17 to listOf(34.9, 80.9, 103.9, 126.9, 156.9),
            18 to listOf(35.9, 82.9, 106.9, 130.9, 161.9),
            19 to listOf(38.9, 90.9, 117.9, 146.9, 178.9),
            20 to listOf(39.9, 92.9, 118.9, 150.9, 184.9),
            21 to listOf(39.9, 93.9, 119.9, 146.9, 177.9),
            //age 40 - 49
            22 to listOf(22.9, 53.9, 69.9, 84.9, 111.9),
            23 to listOf(28.9, 66.9, 85.9, 104.9, 132.9),
            24 to listOf(29.9, 68.9, 88.9, 108.9, 133.9),
            25 to listOf(31.9, 73.9, 94.9, 115.9, 142.9),
            26 to listOf(32.9, 77.9, 100.9, 125.9, 152.9),
            27 to listOf(34.9, 80.9, 103.9, 131.9, 159.9),
            28 to listOf(34.9, 81.9, 105.9, 128.9, 155.9)
        )

        val thresholds = strengthLevels[classLvl] ?: return "Unknown"

        return when {
            oneRepMax <= thresholds[0] -> "Inactive"
            oneRepMax <= thresholds[1] -> "Physically Active"
            oneRepMax <= thresholds[2] -> "Beginner"
            oneRepMax <= thresholds[3] -> "Intermediate"
            oneRepMax <= thresholds[4] -> "Advanced"
            else -> "Elite"
        }
    }

    // Press

    fun getStrPressLvlMaleKg (age: Int, weight: Int, oneRepMax: Double): String {
        val classLvl = getClassLvl(age, weight)

        val strengthLevels = mapOf(
            // age 15 - 19
            1 to listOf(17.9, 25.9, 32.9, 40.9, 52.9),
            2 to listOf(21.9, 30.9, 39.9, 48.9, 63.9),
            3 to listOf(23.9, 33.9, 43.9, 53.9, 69.9),
            4 to listOf(27.9, 38.9, 49.9, 60.9, 80.9),
            5 to listOf(28.9, 39.9, 50.9, 62.9, 82.9),
            6 to listOf(28.9, 40.9, 52.9, 64.9, 84.9),
            7 to listOf(30.9, 42.9, 55.9, 68.9, 89.9),
            // age 20 - 29
            8 to listOf(22.9, 31.9, 41.9, 50.9, 66.9),
            9 to listOf(27.9, 38.9, 50.9, 61.9, 81.9),
            10 to listOf(30.9, 42.9, 54.9, 67.9, 88.9),
            11 to listOf(34.9, 48.9, 62.9, 77.9, 101.9),
            12 to listOf(35.9, 49.9, 64.9, 79.9, 104.9),
            13 to listOf(36.9, 51.9, 66.9, 81.9, 107.9),
            14 to listOf(38.9, 54.9, 69.9, 86.9, 113.9),
            // age 30 - 39
            15 to listOf(24.9, 34.9, 44.9, 54.9, 71.9),
            16 to listOf(29.9, 41.9, 53.9, 66.9, 87.9),
            17 to listOf(32.9, 45.9, 58.9, 72.9, 95.9),
            18 to listOf(37.9, 52.9, 67.9, 83.9, 108.9),
            19 to listOf(38.9, 53.9, 69.9, 85.9, 111.9),
            20 to listOf(39.9, 55.9, 71.9, 88.9, 115.9),
            21 to listOf(41.9, 58.9, 75.9, 92.9, 121.9),
            // age 40 - 49
            22 to listOf(20.9, 29.9, 37.9, 46.9, 61.9),
            23 to listOf(25.9, 35.9, 46.9, 57.9, 74.9),
            24 to listOf(27.9, 39.9, 50.9, 62.9, 81.9),
            25 to listOf(31.9, 44.9, 57.9, 71.9, 93.9),
            26 to listOf(32.9, 46.9, 59.9, 73.9, 96.9),
            27 to listOf(33.9, 47.9, 61.9, 75.9, 99.9),
            28 to listOf(35.9, 50.9, 64.9, 79.9, 104.9)
        )

        val thresholds = strengthLevels[classLvl] ?: return "Unknown"

        return when {
            oneRepMax <= thresholds[0] -> "Inactive"
            oneRepMax <= thresholds[1] -> "Physically Active"
            oneRepMax <= thresholds[2] -> "Beginner"
            oneRepMax <= thresholds[3] -> "Intermediate"
            oneRepMax <= thresholds[4] -> "Advanced"
            else -> "Elite"
        }
    }

    fun getStrPressLvlFemaleKg (age: Int, weight: Int, oneRepMax: Double): String {
        val classLvl = getClassLvl(age, weight)

        val strengthLevels = mapOf(
            //age 15 - 19
            1 to listOf(10.9, 13.9, 17.9, 22.9, 30.9),
            2 to listOf(12.9, 16.9, 19.9, 26.9, 36.9),
            3 to listOf(13.9, 17.9, 21.9, 28.9, 39.9),
            4 to listOf(14.9, 20.9, 23.9, 31.9, 42.9),
            5 to listOf(15.9, 21.9, 25.9, 34.9, 45.9),
            6 to listOf(16.9, 23.9, 27.9, 36.9, 47.9),
            7 to listOf(18.9, 26.9, 30.9, 40.9, 51.9),
            //age 20 - 29
            8 to listOf(13.9, 17.9, 21.9, 28.9, 38.9),
            9 to listOf(15.9, 20.9, 25.9, 34.9, 46.9),
            10 to listOf(17.9, 22.9, 27.9, 36.9, 49.9),
            11 to listOf(18.9, 25.9, 30.9, 39.9, 53.9),
            12 to listOf(19.9, 27.9, 32.9, 44.9, 58.9),
            13 to listOf(20.9, 30.9, 34.9, 46.9, 59.9),
            14 to listOf(24.9, 33.9, 38.9, 51.9, 67.9),
            //age 30 - 39
            15 to listOf(15.9, 18.9, 23.9, 31.9, 41.9),
            16 to listOf(17.9, 22.9, 27.9, 36.9, 49.9),
            17 to listOf(18.9, 24.9, 29.9, 39.9, 53.9),
            18 to listOf(19.9, 27.9, 32.9, 42.9, 57.9),
            19 to listOf(21.9, 29.9, 34.9, 47.9, 62.9),
            20 to listOf(22.9, 32.9, 37.9, 49.9, 64.9),
            21 to listOf(26.9, 36.9, 41.9, 55.9, 70.9),
            //age 40 - 49
            22 to listOf(12.9, 15.9, 20.9, 26.9, 35.9),
            23 to listOf(14.9, 18.9, 23.9, 30.9, 42.9),
            24 to listOf(15.9, 20.9, 25.9, 33.9, 46.9),
            25 to listOf(16.9, 23.9, 27.9, 36.9, 49.9),
            26 to listOf(18.9, 25.9, 29.9, 40.9, 53.9),
            27 to listOf(19.9, 27.9, 32.9, 42.9, 55.9),
            28 to listOf(22.9, 30.9, 35.9, 47.9, 60.9)
        )

        val thresholds = strengthLevels[classLvl] ?: return "Unknown"

        return when {
            oneRepMax <= thresholds[0] -> "Inactive"
            oneRepMax <= thresholds[1] -> "Physically Active"
            oneRepMax <= thresholds[2] -> "Beginner"
            oneRepMax <= thresholds[3] -> "Intermediate"
            oneRepMax <= thresholds[4] -> "Advanced"
            else -> "Elite"
        }
    }


    // <-----------------------------------------------------------------------------------------------------------------------> //


    fun assGoalsLvl (age: Int, weight: Double, gender: String, weightUnit: Int, exerType: Int): Map<String, Int> {
        val ageLvl = getAgeLvl(age)
        val adjustedWeight: Double

        if (weightUnit == 1) adjustedWeight = weight / 2.205 else adjustedWeight = weight

        val weightLvl = getWeightLvl(adjustedWeight)

        if(exerType == 1){ // Bench Press
            if (weightUnit == 1) {
                return when { // Kilograms
                    gender == "Male" -> getBenchPressGoalsMaleKg(ageLvl, weightLvl)
                    else -> getBenchPressGoalsFemaleKg(ageLvl, weightLvl)
                }
            } else { // Pounds
                return when {
                    gender == "Male" -> getBenchPressGoalsMaleLbs(ageLvl, weightLvl)
                    else -> getBenchPressGoalsFemaleLbs(ageLvl, weightLvl)
                }
            }
        } else if (exerType == 2){ // Deadlift
            if (weightUnit == 1) {
                return when { // Kilograms
                    gender == "Male" -> getDeadliftGoalsMaleKg(ageLvl, weightLvl)
                    else -> getDeadliftGoalsFemaleKg(ageLvl, weightLvl)
                }
            } else { // Pounds
                return when {
                    gender == "Male" -> getDeadliftGoalsMaleLbs(ageLvl, weightLvl)
                    else -> getDeadliftGoalsFemaleLbs(ageLvl, weightLvl)
                }
            }

        } else { // Press
            if (weightUnit == 1) { // Kilograms
                return when {
                    gender == "Male" -> getPressGoalsMaleKg(ageLvl, weightLvl)
                    else -> getPressGoalsFemaleKg(ageLvl, weightLvl)
                }
            } else { // Pounds
                return when {
                    gender == "Male" -> getPressGoalsMaleLbs(ageLvl, weightLvl)
                    else -> getPressGoalsFemaleLbs(ageLvl, weightLvl)
                }
            }
        }

    }

    // Bench Press

    fun getBenchPressGoalsMaleKg(age: Int, weight: Int): Map<String, Int> {
        val classLvl = getClassLvl(age, weight)

        val strengthLevels = mapOf(
            // age 15 - 19
            1 to listOf(34, 47, 61, 74, 85),
            2 to listOf(42, 59, 75, 92, 107),
            3 to listOf(43, 60, 77, 94, 110),
            4 to listOf(51, 71, 91, 111, 132),
            5 to listOf(53, 74, 95, 116, 134),
            6 to listOf(53, 74, 96, 117, 137),
            7 to listOf(53, 74, 95, 116, 138),
            // age 20 - 29
            8 to listOf(36, 51, 65, 80, 92),
            9 to listOf(50, 70, 90, 110, 130),
            10 to listOf(50, 70, 90, 110, 150),
            11 to listOf(52, 73, 93, 114, 161),
            12 to listOf(55, 77, 99, 121, 170),
            13 to listOf(59, 82, 106, 129, 181),
            14 to listOf(61, 85, 109, 133, 182),
            // age 30 - 39
            15 to listOf(39, 55, 71, 87, 114),
            16 to listOf(53, 75, 96, 117, 155),
            17 to listOf(54, 75, 97, 119, 156),
            18 to listOf(59, 83, 107, 131, 173),
            19 to listOf(61, 84, 108, 135, 176),
            20 to listOf(63, 85, 110, 139, 177),
            21 to listOf(67, 94, 121, 148, 195),
            // age 40 - 49
            22 to listOf(37, 52, 66, 81, 105),
            23 to listOf(41, 57, 73, 89, 116),
            24 to listOf(48, 67, 86, 105, 135),
            25 to listOf(54, 75, 97, 118, 143),
            26 to listOf(56, 78, 100, 122, 154),
            27 to listOf(58, 81, 104, 127, 164),
            28 to listOf(62, 87, 111, 136, 166)
        )

        val thresholds = strengthLevels[classLvl] ?: return emptyMap()

        return mapOf(
            "Physically Active" to thresholds[0],
            "Beginner" to thresholds[1],
            "Intermediate" to thresholds[2],
            "Advanced" to thresholds[3],
            "Elite" to thresholds[4]
        )
    }

    fun getBenchPressGoalsFemaleKg(age: Int, weight: Int): Map<String, Int> {
        val classLvl = getClassLvl(age, weight)

        val strengthLevels = mapOf(
            // age 15 - 19
            1 to listOf(14, 19, 27, 37, 51),
            2 to listOf(21, 28, 39, 54, 75),
            3 to listOf(19, 27, 37, 51, 70),
            4 to listOf(21, 28, 39, 54, 75),
            5 to listOf(22, 30, 41, 57, 78),
            6 to listOf(22, 31, 42, 58, 81),
            7 to listOf(23, 32, 44, 61, 84),
            // age 20 - 29
            8 to listOf(14, 19, 26, 36, 49),
            9 to listOf(19, 27, 37, 51, 70),
            10 to listOf(21, 29, 40, 56, 77),
            11 to listOf(22, 30, 42, 57, 79),
            12 to listOf(25, 34, 47, 64, 89),
            13 to listOf(25, 35, 48, 66, 91),
            14 to listOf(28, 39, 54, 75, 103),
            // age 30 - 39
            15 to listOf(12, 17, 22, 27, 57),
            16 to listOf(30, 41, 53, 65, 83),
            17 to listOf(33, 47, 60, 73, 89),
            18 to listOf(35, 51, 66, 80, 98),
            19 to listOf(38, 51, 67, 84, 100),
            20 to listOf(39, 52, 66, 86, 102),
            21 to listOf(41, 58, 74, 90, 106),
            // age 40 - 49
            22 to listOf(14, 15, 25, 25, 46),
            23 to listOf(20, 31, 36, 56, 66),
            24 to listOf(23, 37, 41, 67, 75),
            25 to listOf(24, 44, 44, 80, 81),
            26 to listOf(26, 32, 57, 59, 85),
            27 to listOf(26, 41, 48, 75, 87),
            28 to listOf(28, 44, 51, 79, 93)
        )

        val thresholds = strengthLevels[classLvl] ?: return emptyMap()

        return mapOf(
            "Physically Active" to thresholds[0],
            "Beginner" to thresholds[1],
            "Intermediate" to thresholds[2],
            "Advanced" to thresholds[3],
            "Elite" to thresholds[4]
        )
    }

    fun getBenchPressGoalsMaleLbs(age: Int, weight: Int): Map<String, Int> {
        val classLvl = getClassLvl(age, weight)

        val strengthLevels = mapOf(
            // age 15 - 19
            1 to listOf(74, 104, 134, 164, 186),
            2 to listOf(92, 129, 166, 203, 236),
            3 to listOf(94, 132, 170, 207, 242),
            4 to listOf(112, 156, 201, 246, 290),
            5 to listOf(116, 162, 208, 255, 295),
            6 to listOf(117, 164, 211, 258, 301),
            7 to listOf(116, 162, 208, 255, 304),

            // age 20 - 29
            8 to listOf(80, 112, 144, 176, 202),
            9 to listOf(110, 154, 198, 243, 287),
            10 to listOf(110, 154, 198, 243, 331),
            11 to listOf(114, 160, 206, 252, 354),
            12 to listOf(121, 170, 218, 267, 375),
            13 to listOf(130, 181, 233, 285, 400),
            14 to listOf(134, 187, 241, 294, 401),

            // age 30 - 39
            15 to listOf(87, 121, 156, 191, 251),
            16 to listOf(117, 164, 211, 258, 341),
            17 to listOf(119, 166, 214, 261, 345),
            18 to listOf(129, 184, 236, 289, 381),
            19 to listOf(134, 184, 238, 297, 388),
            20 to listOf(139, 188, 242, 307, 390),
            21 to listOf(148, 207, 266, 325, 429),

            // age 40 - 49
            22 to listOf(81, 114, 146, 179, 231),
            23 to listOf(90, 125, 161, 197, 255),
            24 to listOf(105, 147, 188, 230, 298),
            25 to listOf(118, 166, 213, 261, 316),
            26 to listOf(123, 172, 221, 270, 338),
            27 to listOf(127, 177, 228, 279, 361),
            28 to listOf(136, 191, 246, 300, 367)
        )

        val thresholds = strengthLevels[classLvl] ?: return emptyMap()

        return mapOf(
            "Physically Active" to thresholds[0],
            "Beginner" to thresholds[1],
            "Intermediate" to thresholds[2],
            "Advanced" to thresholds[3],
            "Elite" to thresholds[4]
        )
    }

    fun getBenchPressGoalsFemaleLbs(age: Int, weight: Int): Map<String, Int> {
        val classLvl = getClassLvl(age, weight)

        val strengthLevels = mapOf(
            // age 15 - 19
            1 to listOf(31, 43, 59, 81, 112),
            2 to listOf(45, 63, 86, 119, 164),
            3 to listOf(43, 59, 81, 112, 155),
            4 to listOf(45, 63, 86, 119, 164),
            5 to listOf(48, 66, 91, 125, 173),
            6 to listOf(49, 68, 93, 129, 177),
            7 to listOf(51, 71, 97, 134, 185),

            // age 20 - 29
            8 to listOf(30, 41, 57, 79, 108),
            9 to listOf(43, 59, 81, 112, 154),
            10 to listOf(47, 64, 89, 123, 169),
            11 to listOf(48, 66, 92, 126, 174),
            12 to listOf(54, 75, 103, 142, 196),
            13 to listOf(56, 77, 106, 146, 201),
            14 to listOf(63, 87, 120, 165, 227),

            // age 30 - 39
            15 to listOf(27, 38, 49, 60, 126),
            16 to listOf(65, 91, 117, 143, 182),
            17 to listOf(73, 103, 132, 162, 197),
            18 to listOf(78, 112, 145, 177, 216),
            19 to listOf(84, 113, 147, 186, 220),
            20 to listOf(86, 114, 146, 190, 225),
            21 to listOf(91, 127, 163, 199, 234),

            // age 40 - 49
            22 to listOf(30, 32, 55, 55, 100),
            23 to listOf(44, 68, 80, 124, 145),
            24 to listOf(50, 82, 91, 149, 165),
            25 to listOf(54, 97, 98, 176, 178),
            26 to listOf(57, 71, 103, 130, 187),
            27 to listOf(58, 91, 106, 166, 192),
            28 to listOf(62, 96, 113, 175, 205)
        )

        val thresholds = strengthLevels[classLvl] ?: return emptyMap()

        return mapOf(
            "Physically Active" to thresholds[0],
            "Beginner" to thresholds[1],
            "Intermediate" to thresholds[2],
            "Advanced" to thresholds[3],
            "Elite" to thresholds[4]
        )
    }


    // Deadlift

    fun getDeadliftGoalsMaleKg(age: Int, weight: Int): Map<String, Int> {
        val classLvl = getClassLvl(age, weight)

        val strengthLevels = mapOf(
            // age 15 - 19
            1 to listOf(45, 74, 93, 103, 115),
            2 to listOf(58, 97, 121, 134, 149),
            3 to listOf(73, 122, 152, 169, 188),
            4 to listOf(77, 129, 161, 179, 199),
            5 to listOf(81, 135, 169, 188, 209),
            6 to listOf(82, 137, 172, 191, 212),
            7 to listOf(84, 139, 174, 194, 215),
            // age 20 - 29
            8 to listOf(47, 81, 101, 127, 141),
            9 to listOf(64, 112, 139, 186, 207),
            10 to listOf(76, 131, 164, 219, 243),
            11 to listOf(83, 144, 180, 240, 266),
            12 to listOf(84, 146, 182, 243, 270),
            13 to listOf(85, 147, 184, 245, 273),
            14 to listOf(86, 149, 187, 249, 276),
            // age 30 - 39
            15 to listOf(51, 89, 112, 149, 175),
            16 to listOf(64, 112, 140, 186, 219),
            17 to listOf(76, 132, 165, 220, 259),
            18 to listOf(81, 142, 177, 236, 278),
            19 to listOf(83, 145, 181, 242, 284),
            20 to listOf(84, 146, 183, 244, 287),
            21 to listOf(89, 154, 192, 257, 302),
            // age 40 - 49
            22 to listOf(46, 80, 100, 118, 131),
            23 to listOf(53, 92, 115, 154, 181),
            24 to listOf(63, 109, 137, 182, 214),
            25 to listOf(65, 113, 141, 188, 222),
            26 to listOf(71, 124, 154, 206, 242),
            27 to listOf(75, 130, 162, 217, 255),
            28 to listOf(76, 133, 166, 221, 260)
        )

        val thresholds = strengthLevels[classLvl] ?: return emptyMap()

        return mapOf(
            "Physically Active" to thresholds[0],
            "Beginner" to thresholds[1],
            "Intermediate" to thresholds[2],
            "Advanced" to thresholds[3],
            "Elite" to thresholds[4]
        )
    }

    fun getDeadliftGoalsFemaleKg(age: Int, weight: Int): Map<String, Int> {
        val classLvl = getClassLvl(age, weight)

        val strengthLevels = mapOf(
            // age 15 - 19
            1 to listOf(17, 39, 51, 62, 82),
            2 to listOf(22, 51, 65, 80, 102),
            3 to listOf(24, 56, 72, 88, 108),
            4 to listOf(25, 59, 75, 92, 113),
            5 to listOf(26, 61, 79, 99, 118),
            6 to listOf(27, 62, 80, 103, 123),
            7 to listOf(30, 69, 89, 109, 129),

            // age 20 - 29
            8 to listOf(25, 58, 74, 91, 120),
            9 to listOf(30, 70, 90, 110, 140),
            10 to listOf(33, 77, 99, 121, 150),
            11 to listOf(34, 79, 101, 124, 154),
            12 to listOf(35, 82, 107, 133, 162),
            13 to listOf(35, 82, 106, 134, 164),
            14 to listOf(36, 84, 108, 132, 159),

            // age 30 - 39
            15 to listOf(30, 70, 90, 110, 145),
            16 to listOf(32, 74, 96, 117, 149),
            17 to listOf(35, 81, 104, 127, 157),
            18 to listOf(36, 83, 107, 131, 162),
            19 to listOf(39, 91, 118, 147, 179),
            20 to listOf(40, 93, 119, 151, 185),
            21 to listOf(40, 94, 120, 147, 178),

            // age 40 - 49
            22 to listOf(23, 54, 70, 85, 112),
            23 to listOf(29, 67, 86, 105, 133),
            24 to listOf(30, 69, 89, 109, 134),
            25 to listOf(32, 74, 95, 116, 143),
            26 to listOf(33, 78, 101, 126, 153),
            27 to listOf(35, 81, 104, 132, 160),
            28 to listOf(35, 82, 106, 129, 156)
        )

        val thresholds = strengthLevels[classLvl] ?: return emptyMap()

        return mapOf(
            "Physically Active" to thresholds[0],
            "Beginner" to thresholds[1],
            "Intermediate" to thresholds[2],
            "Advanced" to thresholds[3],
            "Elite" to thresholds[4]
        )
    }

    fun getDeadliftGoalsMaleLbs(age: Int, weight: Int): Map<String, Int> {
        val classLvl = getClassLvl(age, weight)

        val strengthLevels = mapOf(
            // age 15 - 19
            1 to listOf(98, 164, 204, 227, 252),
            2 to listOf(128, 213, 266, 296, 328),
            3 to listOf(161, 268, 335, 372, 414),
            4 to listOf(171, 284, 355, 395, 439),
            5 to listOf(179, 298, 373, 414, 460),
            6 to listOf(182, 303, 379, 421, 467),
            7 to listOf(185, 308, 384, 427, 475),

            // age 20 - 29
            8 to listOf(103, 179, 223, 279, 310),
            9 to listOf(141, 246, 307, 410, 455),
            10 to listOf(167, 290, 362, 483, 536),
            11 to listOf(182, 317, 396, 528, 587),
            12 to listOf(185, 321, 402, 536, 595),
            13 to listOf(187, 325, 406, 541, 601),
            14 to listOf(189, 329, 411, 548, 609),

            // age 30 - 39
            15 to listOf(113, 197, 246, 328, 386),
            16 to listOf(142, 246, 308, 410, 483),
            17 to listOf(167, 291, 363, 484, 570),
            18 to listOf(180, 312, 390, 520, 612),
            19 to listOf(184, 320, 399, 533, 627),
            20 to listOf(185, 322, 403, 537, 632),
            21 to listOf(195, 339, 424, 566, 665),

            // age 40 - 49
            22 to listOf(101, 176, 220, 259, 288),
            23 to listOf(117, 203, 254, 339, 398),
            24 to listOf(138, 241, 301, 401, 472),
            25 to listOf(143, 249, 311, 415, 488),
            26 to listOf(157, 272, 340, 454, 534),
            27 to listOf(165, 286, 358, 477, 562),
            28 to listOf(168, 293, 366, 488, 574)
        )

        val thresholds = strengthLevels[classLvl] ?: return emptyMap()

        return mapOf(
            "Physically Active" to thresholds[0],
            "Beginner" to thresholds[1],
            "Intermediate" to thresholds[2],
            "Advanced" to thresholds[3],
            "Elite" to thresholds[4]
        )
    }

    fun getDeadliftGoalsFemaleLbs(age: Int, weight: Int): Map<String, Int> {
        val classLvl = getClassLvl(age, weight)

        val strengthLevels = mapOf(
            // age 15 - 19
            1 to listOf(37, 87, 112, 136, 180),
            2 to listOf(48, 112, 144, 176, 224),
            3 to listOf(53, 123, 159, 194, 238),
            4 to listOf(55, 129, 166, 203, 250),
            5 to listOf(56, 133, 173, 217, 260),
            6 to listOf(59, 137, 176, 226, 272),
            7 to listOf(65, 152, 196, 239, 285),

            // age 20 - 29
            8 to listOf(55, 127, 164, 200, 264),
            9 to listOf(66, 154, 198, 242, 309),
            10 to listOf(73, 170, 218, 267, 331),
            11 to listOf(74, 174, 223, 273, 338),
            12 to listOf(77, 182, 235, 293, 357),
            13 to listOf(78, 181, 233, 296, 360),
            14 to listOf(79, 185, 238, 291, 351),

            // age 30 - 39
            15 to listOf(66, 154, 198, 243, 320),
            16 to listOf(70, 164, 211, 258, 328),
            17 to listOf(76, 177, 228, 279, 346),
            18 to listOf(79, 183, 236, 288, 358),
            19 to listOf(85, 201, 260, 323, 395),
            20 to listOf(88, 204, 263, 332, 407),
            21 to listOf(88, 206, 265, 324, 393),

            // age 40 - 49
            22 to listOf(51, 120, 154, 188, 248),
            23 to listOf(63, 147, 188, 230, 293),
            24 to listOf(65, 152, 196, 239, 296),
            25 to listOf(69, 162, 208, 255, 315),
            26 to listOf(73, 172, 223, 278, 337),
            27 to listOf(76, 177, 228, 290, 353),
            28 to listOf(78, 181, 233, 285, 343)
        )

        val thresholds = strengthLevels[classLvl] ?: return emptyMap()

        return mapOf(
            "Physically Active" to thresholds[0],
            "Beginner" to thresholds[1],
            "Intermediate" to thresholds[2],
            "Advanced" to thresholds[3],
            "Elite" to thresholds[4]
        )
    }

    // Press

    fun getPressGoalsMaleKg(age: Int, weight: Int): Map<String, Int> {
        val classLvl = getClassLvl(age, weight)

        val strengthLevels = mapOf(
            // age 15 - 19
            1 to listOf(18, 26, 33, 41, 53),
            2 to listOf(22, 31, 40, 49, 64),
            3 to listOf(24, 34, 44, 54, 70),
            4 to listOf(28, 39, 50, 61, 81),
            5 to listOf(29, 40, 51, 63, 83),
            6 to listOf(29, 41, 53, 65, 85),
            7 to listOf(31, 43, 56, 69, 90),
            // age 20 - 29
            8 to listOf(23, 32, 42, 51, 67),
            9 to listOf(28, 39, 51, 62, 82),
            10 to listOf(31, 43, 55, 68, 89),
            11 to listOf(35, 49, 63, 78, 102),
            12 to listOf(36, 50, 65, 80, 105),
            13 to listOf(37, 52, 67, 82, 108),
            14 to listOf(39, 55, 70, 87, 114),
            // age 30 - 39
            15 to listOf(25, 35, 45, 55, 72),
            16 to listOf(30, 42, 54, 67, 88),
            17 to listOf(33, 46, 59, 73, 96),
            18 to listOf(38, 53, 68, 84, 109),
            19 to listOf(39, 54, 70, 86, 112),
            20 to listOf(40, 56, 72, 89, 116),
            21 to listOf(42, 59, 76, 93, 122),
            // age 40 - 49
            22 to listOf(21, 30, 38, 47, 62),
            23 to listOf(26, 36, 47, 58, 75),
            24 to listOf(28, 40, 51, 63, 82),
            25 to listOf(32, 45, 58, 72, 94),
            26 to listOf(33, 47, 60, 74, 97),
            27 to listOf(34, 48, 62, 76, 100),
            28 to listOf(36, 51, 65, 80, 105)
        )

        val thresholds = strengthLevels[classLvl] ?: return emptyMap()

        return mapOf(
            "Physically Active" to thresholds[0],
            "Beginner" to thresholds[1],
            "Intermediate" to thresholds[2],
            "Advanced" to thresholds[3],
            "Elite" to thresholds[4]
        )
    }

    fun getPressGoalsFemaleKg(age: Int, weight: Int): Map<String, Int> {
        val classLvl = getClassLvl(age, weight)

        val strengthLevels = mapOf(
            //age 15 - 19
            1 to listOf(11, 14, 18, 23, 31),
            2 to listOf(13, 17, 20, 27, 37),
            3 to listOf(14, 18, 22, 29, 40),
            4 to listOf(15, 21, 24, 32, 43),
            5 to listOf(16, 22, 26, 35, 46),
            6 to listOf(17, 24, 28, 37, 48),
            7 to listOf(19, 27, 31, 41, 52),
            // age 20 - 29
            8 to listOf(14, 18, 22, 29, 39),
            9 to listOf(16, 21, 26, 35, 47),
            10 to listOf(18, 23, 28, 37, 50),
            11 to listOf(19, 26, 31, 40, 54),
            12 to listOf(20, 28, 33, 45, 59),
            13 to listOf(21, 31, 35, 47, 60),
            14 to listOf(25, 34, 39, 52, 68),
            // age 30 - 39
            15 to listOf(16, 19, 24, 32, 42),
            16 to listOf(18, 23, 28, 37, 50),
            17 to listOf(19, 25, 30, 40, 54),
            18 to listOf(20, 28, 33, 43, 58),
            19 to listOf(22, 30, 35, 48, 63),
            20 to listOf(23, 33, 38, 50, 65),
            21 to listOf(27, 37, 42, 56, 71),
            // age 40 - 49
            22 to listOf(13, 16, 21, 27, 36),
            23 to listOf(15, 19, 24, 31, 43),
            24 to listOf(16, 21, 26, 34, 47),
            25 to listOf(17, 24, 28, 37, 50),
            26 to listOf(19, 26, 30, 41, 54),
            27 to listOf(20, 28, 33, 43, 56),
            28 to listOf(23, 31, 36, 48, 61)
        )

        val thresholds = strengthLevels[classLvl] ?: return emptyMap()

        return mapOf(
            "Physically Active" to thresholds[0],
            "Beginner" to thresholds[1],
            "Intermediate" to thresholds[2],
            "Advanced" to thresholds[3],
            "Elite" to thresholds[4]
        )
    }

    fun getPressGoalsMaleLbs(age: Int, weight: Int): Map<String, Int> {
        val classLvl = getClassLvl(age, weight)

        val strengthLevels = mapOf(
            // age 15 - 19
            1 to listOf(41, 56, 73, 89, 117),
            2 to listOf(49, 68, 88, 109, 142),
            3 to listOf(54, 75, 96, 119, 155),
            4 to listOf(61, 85, 110, 135, 177),
            5 to listOf(63, 88, 113, 139, 182),
            6 to listOf(65, 90, 116, 144, 188),
            7 to listOf(69, 95, 123, 151, 198),
            // age 20 - 29
            8 to listOf(51, 71, 92, 113, 148),
            9 to listOf(62, 87, 111, 137, 180),
            10 to listOf(68, 95, 122, 150, 196),
            11 to listOf(78, 108, 139, 171, 224),
            12 to listOf(80, 111, 143, 176, 231),
            13 to listOf(82, 114, 147, 182, 238),
            14 to listOf(87, 121, 155, 192, 251),
            // age 30 - 39
            15 to listOf(55, 77, 99, 122, 159),
            16 to listOf(67, 93, 120, 148, 193),
            17 to listOf(73, 102, 131, 161, 211),
            18 to listOf(83, 116, 149, 184, 241),
            19 to listOf(86, 119, 154, 189, 248),
            20 to listOf(88, 123, 158, 195, 256),
            21 to listOf(93, 130, 167, 206, 270),
            // age 40 - 49
            22 to listOf(47, 66, 85, 105, 137),
            23 to listOf(57, 80, 103, 127, 166),
            24 to listOf(63, 87, 112, 139, 181),
            25 to listOf(72, 100, 128, 158, 207),
            26 to listOf(74, 103, 132, 163, 213),
            27 to listOf(76, 106, 136, 168, 220),
            28 to listOf(80, 112, 144, 177, 232)
        )

        val thresholds = strengthLevels[classLvl] ?: return emptyMap()

        return mapOf(
            "Physically Active" to thresholds[0],
            "Beginner" to thresholds[1],
            "Intermediate" to thresholds[2],
            "Advanced" to thresholds[3],
            "Elite" to thresholds[4]
        )
    }

    fun getPressGoalsFemaleLbs(age: Int, weight: Int): Map<String, Int> {
        val classLvl = getClassLvl(age, weight)

        val strengthLevels = mapOf(
            //age 15 - 19
            1 to listOf(25, 31, 39, 51, 68),
            2 to listOf(28, 36, 45, 59, 81),
            3 to listOf(31, 41, 49, 65, 88),
            4 to listOf(32, 45, 53, 70, 94),
            5 to listOf(36, 49, 57, 78, 102),
            6 to listOf(37, 53, 62, 81, 105),
            7 to listOf(43, 59, 67, 90, 114),
            // age 20 - 29
            8 to listOf(32, 39, 49, 65, 86),
            9 to listOf(36, 46, 56, 75, 103),
            10 to listOf(39, 51, 62, 82, 111),
            11 to listOf(41, 57, 68, 88, 119),
            12 to listOf(45, 62, 72, 98, 139),
            13 to listOf(47, 68, 78, 103, 133),
            14 to listOf(54, 75, 85, 114, 145),
            // age 30 - 39
            15 to listOf(34, 42, 53, 69, 93),
            16 to listOf(39, 50, 61, 80, 110),
            17 to listOf(42, 55, 66, 88, 119),
            18 to listOf(44, 62, 73, 95, 128),
            19 to listOf(49, 66, 77, 106, 139),
            20 to listOf(51, 73, 84, 110, 143),
            21 to listOf(58, 80, 91, 122, 155),
            // age 40 - 49
            22 to listOf(29, 36, 45, 60, 80),
            23 to listOf(33, 43, 52, 69, 95),
            24 to listOf(36, 47, 57, 76, 102),
            25 to listOf(38, 53, 62, 81, 110),
            26 to listOf(42, 57, 66, 91, 119),
            27 to listOf(44, 62, 72, 95, 123),
            28 to listOf(50, 69, 79, 105, 134)
        )

        val thresholds = strengthLevels[classLvl] ?: return emptyMap()

        return mapOf(
            "Physically Active" to thresholds[0],
            "Beginner" to thresholds[1],
            "Intermediate" to thresholds[2],
            "Advanced" to thresholds[3],
            "Elite" to thresholds[4]
        )
    }

}
