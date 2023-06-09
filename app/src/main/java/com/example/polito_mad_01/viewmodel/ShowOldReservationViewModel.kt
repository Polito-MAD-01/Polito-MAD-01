package com.example.polito_mad_01.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.example.polito_mad_01.model.*
import com.example.polito_mad_01.repositories.ReservationRepository

class ShowOldReservationViewModel(private val repository: ReservationRepository) : ViewModel() {

    fun getOldReservationById(id: Int): LiveData<Slot> {
        return repository.getReservationById(id)
    }

    fun getSportImage(playgroundId: Int): LiveData<Uri?> {
        return repository.getSportImage(playgroundId)
    }
}

class ShowOldReservationViewModelFactory(private val repository: ReservationRepository) :
    ViewModelProvider.Factory{
    override fun <T: ViewModel> create(modelClass: Class<T>): T{
        if (modelClass.isAssignableFrom(ShowOldReservationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShowOldReservationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}