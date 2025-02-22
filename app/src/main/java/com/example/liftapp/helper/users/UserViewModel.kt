package com.example.liftapp.helper.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserViewModel : ViewModel() {

    private val userProfileHelper = UserProfileHelper()

    private val _name = MutableLiveData<String>()
    private val _age = MutableLiveData<Int>()
    private val _weight = MutableLiveData<Double>()
    private val _gender = MutableLiveData<String?>()
    private val _unit = MutableLiveData<Int>()
    private val _email = MutableLiveData<String?>() // New email field

    val name: LiveData<String> get() = _name
    val age: LiveData<Int> get() = _age
    val weight: LiveData<Double> get() = _weight
    val gender: LiveData<String?> get() = _gender
    val unit: LiveData<Int> get() = _unit
    val email: LiveData<String?> get() = _email // Expose the email LiveData

    fun setName(value: String) { _name.value = value }
    fun setAge(value: Int) { _age.value = value }
    fun setWeight(value: Double) { _weight.value = value }
    fun setGender(value: String?) { _gender.value = value }
    fun setUnit(value: Int) { _unit.value = value }
    fun setEmail(value: String?) { _email.value = value } // Setter for email
    fun fetchUserData(userId: String) {
        userProfileHelper.fetchUserData(userId) { user ->
            if (user != null) {
                setName(user.name)
                setAge(user.age)
                setWeight(user.weight)
                setGender(user.gender)
                setUnit(user.unit)
            }
        }
    }
}