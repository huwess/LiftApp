package com.example.liftapp.helper.record

data class StrengthRecord(
    val repetitions: Int = 0,
    val dumbbellWeight: Double = 0.0,
    val oneRepMax: Double = 0.0,
    val strengthLevel: String = "",
    val duration: Long,
    val timestamp: MutableMap<String, String>
)
