package com.example.polito_mad_01.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.polito_mad_01.model.*
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

class ReservationRepository() {

    fun getSlotsByUserId(userID : String): LiveData<List<Slot>> {
        val liveDataList = MutableLiveData<List<Slot>>()
        FirebaseFirestore.getInstance().collection("reservations")
            .where(Filter.or(
                    Filter.equalTo("user_id", userID),
                    Filter.equalTo("reserved", false)))
            .addSnapshotListener { r, _ ->
                val list = mutableListOf<Slot>()
                r?.forEach {
                    list += it.toObject(Slot::class.java)
                    liveDataList.value = list
                    println("TEST $it")
                }

            }
        return liveDataList
    }

    fun getReservationById(slotID: Int): LiveData<Slot> {
        val slot = MutableLiveData<Slot>()
        FirebaseFirestore.getInstance().collection("reservations")
            .document(slotID.toString())
            .addSnapshotListener { r, _ ->
                slot.value = r?.toObject(Slot::class.java)
            }
        return slot
    }

    fun getFutureFreeSlots(date: String): LiveData<List<Slot>> {
        println("DATE $date")
        val liveDataList = MutableLiveData<List<Slot>>()
        FirebaseFirestore.getInstance().collection("reservations")
            .where(Filter.and(
                Filter.greaterThan("date", date),
                Filter.equalTo("reserved", false)))
            .addSnapshotListener { r, _ ->
                val list = mutableListOf<Slot>()
                r?.forEach {
                    list += it.toObject(Slot::class.java)
                    liveDataList.value = list
                    println("TEST $it")
                }

            }
        return liveDataList
    }

}