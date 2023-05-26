package com.example.polito_mad_01.viewmodel

import android.view.View
import androidx.lifecycle.*
import com.example.polito_mad_01.model.*
import com.example.polito_mad_01.repositories.UserRepository
import kotlin.concurrent.thread

class EditProfileViewModel(private val userRepository: UserRepository) : ViewModel() {

    lateinit var user : MutableLiveData<User>
    lateinit var chipGroup : LiveData<View>
     var imageUri = MutableLiveData<String>(null)

    fun getUser(): LiveData<User>{
        user = userRepository.getUser() as MutableLiveData<User>
        return user
    }

    fun updateUser() {
        thread {
            println("UPDATE updateUser")
            user.value?.let { userRepository.updateUser(it) }
        }
    }


}


class EditProfileViewModelFactory(private val repository: UserRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}