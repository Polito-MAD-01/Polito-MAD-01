package com.example.polito_mad_01.repositories


import android.accessibilityservice.GestureDescription.StrokeDescription
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.*
import com.example.polito_mad_01.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.net.URI

class UserRepository{

    private val fs = FirebaseFirestore.getInstance()
    private val fAuth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun getUser(): LiveData<User> {
        val userID = fAuth.currentUser?.uid ?: ""
        val user = MutableLiveData<User>()
        fs.collection("users")
            .document(userID)
            .addSnapshotListener { r, _ ->
                user.value =  r?.toObject(User::class.java)
            }
        return user
    }

    fun updateUser(user: User) {
        val userID = fAuth.currentUser?.uid ?: ""
        fs.collection("users")
            .document(userID)
            .set(user, SetOptions.merge())
            //.addOnSuccessListener { println("UPDATE User updated") }
    }

    fun getProfileImage(): LiveData<Uri?> {
        val userID = fAuth.currentUser?.uid ?: throw Exception("User not logged in")
        val storageReference = storage.reference
        val imageRef = storageReference.child("profileImages/$userID.jpg")
        val localFile = File.createTempFile("images", "jpg")
        val image = MutableLiveData<Uri?>()
        imageRef.getFile(localFile).addOnSuccessListener {
            image.value = localFile.toUri()
        }.addOnFailureListener {
            image.value = null
        }
        return image
    }

    fun updateProfileImage(imageUri: Uri) {
        val userID = fAuth.currentUser?.uid ?: throw Exception("User not logged in")
        val storageReference = storage.reference
        val imageRef = storageReference.child("profileImages/$userID.jpg")
        imageRef.putFile(imageUri)
            //.addOnSuccessListener { println("Image uploaded") }
        .addOnFailureListener {
            throw Exception("Error while uploading image")
        }
    }

    fun getFriendsNickname(idList: List<String>): LiveData<List<Pair<String,String>>> {
        val nicknameList = MutableLiveData<List<Pair<String,String>>>()
        fs.collection("users")
            .get()
            .addOnSuccessListener { result ->
                nicknameList.value = result.documents
                    .filter { idList.contains(it.id) }
                    .map { Pair(it.id,it["nickname"].toString()) }
            }
        return nicknameList
    }

    fun createUser(user: User, uuid: String){
        fs.collection("users")
            .document(uuid)
            .set(user)
            .addOnSuccessListener { println("User created") }
    }

    fun addFriend(email: String): LiveData<String> {
        val userID = fAuth.currentUser?.uid ?: throw Exception("User not logged in")
        val toBeReturned = MutableLiveData<String>()
        fs.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    toBeReturned.value = ""
                } else {
                    println("User found ${result.documents[0].id}")
                    fs.collection("friendRequests")
                        .document("${userID}-${result.documents[0].id}")
                        .set(mapOf("sender" to userID, "receiver" to result.documents[0].id))
                        .addOnSuccessListener { println("Friend request sent") }
                    toBeReturned.value = result.documents[0].id
                }
            }
        return toBeReturned
    }

    fun getRequestsUUID(): LiveData<List<String>>{
        val userID = fAuth.currentUser?.uid ?: throw Exception("User not logged in")
        val requests = MutableLiveData<List<String>>()
        fs.collection("friendRequests")
            .whereEqualTo("receiver", userID)
            .addSnapshotListener { result, _ ->
                requests.value = result?.documents?.map { it["sender"].toString() }
            }
        return requests
    }

    fun acceptRequest(senderUUID: String) {
        val userID = fAuth.currentUser?.uid ?: throw Exception("User not logged in")

        fs.collection("users")
            .document(userID)
            .update("friends", FieldValue.arrayUnion(senderUUID))
            .addOnSuccessListener { println("Friend added") }

        fs.collection("users")
            .document(senderUUID)
            .update("friends", FieldValue.arrayUnion(userID))
            .addOnSuccessListener { println("Friend added") }

        fs.collection("friendRequests")
            .document("$senderUUID-$userID")
            .delete()
            .addOnSuccessListener { println("Request deleted") }
    }

    fun declineRequest(senderUUID: String) {
        val userID = fAuth.currentUser?.uid ?: throw Exception("User not logged in")
        fs.collection("friendRequests")
            .document("$senderUUID-$userID")
            .delete()
            .addOnSuccessListener { println("Request deleted") }
    }
}