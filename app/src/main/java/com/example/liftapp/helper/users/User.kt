package com.example.liftapp.helper.users

data class User(
    var name: String = "",
    var age: Int = 0,
    var weight: Double = 0.0,
    var gender: String = "",
    var unit: Int = 0
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "age" to age,
            "weight" to weight,
            "gender" to gender,
            "unit" to unit
        )
    }
}
