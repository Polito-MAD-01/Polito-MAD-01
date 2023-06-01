package com.example.polito_mad_01.repositories


import android.accessibilityservice.GestureDescription.StrokeDescription
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.*
import com.example.polito_mad_01.model.User
import com.example.polito_mad_01.notifications.APIManager
import com.example.polito_mad_01.notifications.NotificationData
import com.example.polito_mad_01.notifications.PushNotification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.net.URI
import kotlin.concurrent.thread

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

    @OptIn(DelicateCoroutinesApi::class)
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
                        .addOnSuccessListener {
                            println("Friend request sent")
                            GlobalScope.launch (Dispatchers.IO){
                                val data = NotificationData("Friend request", "You have a new friend request from ${fAuth.currentUser?.email}", "FriendRequest")
                                val push = PushNotification(data, "/topics/${result.documents[0].id}")
                                sendNotification(push)
                            }

                        }
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

    private val apiManager = APIManager()

     private suspend fun sendNotification(notification: PushNotification) {
        apiManager.postNotification(notification)
    }

    fun subscribeToNotifications() {
        val userID = fAuth.currentUser?.uid ?: throw Exception("User not logged in")

        println("SUB Subscribing to notifications")
        FirebaseMessaging.getInstance().subscribeToTopic(userID)
            .addOnCompleteListener {
                if(it.isSuccessful)
                    println("Subscribed to friend requests notifications")
                else
                    println("Error while subscribing to friend requests notifications ${it.exception}")
            }
        /*FirebaseMessaging.getInstance().subscribeToTopic("$userID")
            .addOnCompleteListener {
                if(it.isSuccessful)
                    println("Subscribed to game requests notifications")
                else if (it.isCanceled)
                    println("Error while subscribing to game requests notifications ${it.exception}")
            }*/
    }

    fun logout() {
        val userID = fAuth.currentUser?.uid ?: throw Exception("User not logged in")
        FirebaseMessaging.getInstance().unsubscribeFromTopic(userID)
            .addOnCompleteListener {
                if(it.isSuccessful)
                    println("Unsubscribed from friend requests notifications")
                else
                    println("Error while unsubscribing from friend requests notifications ${it.exception}")
            }
    }
}