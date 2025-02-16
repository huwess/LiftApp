package com.example.liftapp.helper.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserViewModel : ViewModel() {
    private val _name = MutableLiveData<String>()
    private val _age = MutableLiveData<Int>()
    private val _weight = MutableLiveData<Int>()
    private val _gender = MutableLiveData<String?>()
    private val _unit = MutableLiveData<Int>()

    val name: LiveData<String> get() = _name
    val age: LiveData<Int> get() = _age
    val weight: LiveData<Int> get() = _weight
    val gender: LiveData<String?> get() = _gender
    val unit: LiveData<Int> get() = _unit

    fun setName(value: String) { _name.value = value }
    fun setAge(value: Int) { _age.value = value }
    fun setWeight(value: Int) { _weight.value = value }
    fun setGender(value: String?) { _gender.value = value }
    fun setUnit(value: Int) { _unit.value = value }
}