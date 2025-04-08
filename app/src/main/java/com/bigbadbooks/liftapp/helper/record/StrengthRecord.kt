package com.bigbadbooks.liftapp.helper.record

data class StrengthRecord(
    val repetitions: Int = 0,
    val dumbbellWeight: Double = 0.0,
    val oneRepMax: Double = 0.0,
    val strengthLevel: String = "Untrained",
    val duration: Long = 0L,
    val unit: Int = 0,
    val date: String = "",  // Added date field
    val time: String = ""
)
