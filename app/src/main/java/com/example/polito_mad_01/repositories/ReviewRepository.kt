package com.example.polito_mad_01.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.polito_mad_01.model.Review
import com.example.polito_mad_01.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReviewRepository {

    private val fs = FirebaseFirestore.getInstance()
    private val fAuth = FirebaseAuth.getInstance()
    fun getSingleReview(playgroundId: Int): LiveData<Review> {
        val toReturn = MutableLiveData<Review>()
        val userID = fAuth.currentUser?.uid ?: ""

        fs.collection("reviews")
            .whereEqualTo("user_id",userID)
            .whereEqualTo("playground_id", playgroundId)
            .addSnapshotListener { r, _ ->
                toReturn.value =  r?.firstOrNull()?.toObject(Review::class.java)
            }
        return toReturn
    }

    fun addReview(reviewToAdd: Review){
        FirebaseFirestore.getInstance().collection("reviews")
            .add(reviewToAdd)
    }

}