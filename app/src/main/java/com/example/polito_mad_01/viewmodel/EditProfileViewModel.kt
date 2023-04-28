package com.example.polito_mad_01.viewmodel

import androidx.lifecycle.*
import com.example.polito_mad_01.db.User
import com.example.polito_mad_01.repositories.UserRepository
import kotlin.concurrent.thread

class EditProfileViewModel(private val userRepository: UserRepository) : ViewModel() {

    var user: LiveData<User> = MutableLiveData()

    private val loaded = MutableLiveData<Boolean>(false)

    fun getUser(userId: Int): LiveData<User>{
        if (loaded.value == false) {
            user = userRepository.userById(userId).asLiveData()
            loaded.value = true
            return user;
        }
        return user;
    }

    fun updateUser() {
        thread {
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