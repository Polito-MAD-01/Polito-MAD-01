package com.example.polito_mad_01.repositories


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.polito_mad_01.model.User
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository(private val fs: FirebaseFirestore) {

    fun getUser(userID : String): LiveData<User> {
        val user = MutableLiveData<User>()
        fs.collection("users")
            .document(userID)
            .addSnapshotListener { r, _ ->
                user.value =  r?.toObject(User::class.java)
            }
        return user
    }

}