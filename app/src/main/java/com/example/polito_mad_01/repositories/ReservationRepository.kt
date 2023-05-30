package com.example.polito_mad_01.repositories

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.*
import com.example.polito_mad_01.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class ReservationRepository{
    private val fs = FirebaseFirestore.getInstance()
    private val fAuth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun getSlotsByUserId(): LiveData<List<Slot>> {
        val liveDataList = MutableLiveData<List<Slot>>()
        val userID = fAuth.currentUser?.uid ?: ""
        fs.collection("reservations")
            .where(Filter.or(
                    Filter.equalTo("user_id", userID),
                    Filter.equalTo("reserved", false)))
            .addSnapshotListener { r, _ ->
                val list = mutableListOf<Slot>()
                r?.forEach {
                    list += it.toObject(Slot::class.java)
                    liveDataList.value = list
                }

            }
        return liveDataList
    }

    fun getReservationById(slotID: Int): LiveData<Slot> {
        val slot = MutableLiveData<Slot>()
        val idFormatted = slotID.toString().padStart(3, '0')
        fs.collection("reservations")
            .document(idFormatted)
            .addSnapshotListener { r, _ ->
                slot.value = r?.toObject(Slot::class.java)
            }
        return slot
    }

    fun getFutureFreeSlots(date: String): LiveData<List<Slot>> {
        val liveDataList = MutableLiveData<List<Slot>>()
        fs.collection("reservations")
            .where(Filter.and(
                Filter.greaterThan("date", date),
                Filter.equalTo("reserved", false)))
            .addSnapshotListener { r, _ ->
                val list = mutableListOf<Slot>()
                r?.forEach {
                    list += it.toObject(Slot::class.java)
                    liveDataList.value = list
                }

            }
        return liveDataList
    }

    fun createOrUpdateReservation(slot: Slot) {
        fs.collection("reservations")
            .document(String.format("%03d",slot.slot_id))
            .set(slot, SetOptions.merge())
            .addOnSuccessListener { println("$slot created") }
    }

    fun getAllReservations(): LiveData<List<Slot>> {
        val liveDataList = MutableLiveData<List<Slot>>()
        fs.collection("reservations")
            .addSnapshotListener { r, _ ->
                val list = mutableListOf<Slot>()
                r?.forEach {
                    list += it.toObject(Slot::class.java)
                    liveDataList.value = list
                }
            }
        return liveDataList
    }

    fun getOldReservationsByUserId(date: String): LiveData<List<Slot>> {
        val userID = fAuth.currentUser?.uid ?: ""
        val liveDataList = MutableLiveData<List<Slot>>()
        fs.collection("reservations")
            .where(Filter.and(
                Filter.equalTo("user_id", userID),
                Filter.lessThan("date", date),
                Filter.equalTo("reserved", true)))
            .addSnapshotListener { r, _ ->
                val list = mutableListOf<Slot>()
                r?.forEach {
                    list += it.toObject(Slot::class.java)
                    liveDataList.value = list
                }
            }
        return liveDataList
    }

    fun getSportImage(playgroundId: Int): LiveData<Uri?> {
        val storageReference = storage.reference
        val imageRef = storageReference.child("playgroundImages/$playgroundId.jpg")
        val localFile = File.createTempFile("images", "jpg")
        val image = MutableLiveData<Uri?>()
        imageRef.getFile(localFile).addOnSuccessListener {
            image.value = localFile.toUri()
        }.addOnFailureListener {
            println("Error while downloading image")
            image.value = null
        }
        return image
    }

}