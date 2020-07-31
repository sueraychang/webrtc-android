package com.src.webrtc.android.sample

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.src.webrtc.android.sample.data.Room
import java.util.*

class MainViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var room: Room

    fun createRoom() {
        Log.d(TAG, "createRoom")

        room = Room(UUID.randomUUID().toString(), System.currentTimeMillis())
        val roomsRef = firestore.collection("rooms")
        roomsRef.add(room)
    }

    fun joinRoom() {
        Log.d(TAG, "joinRoom")
        val roomRef = firestore.collection("rooms").document("DFw1wwRYLOAgIk2pHJzm")
        roomRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                } else {
                    Log.e(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "get failed with ", exception)
            }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared")
    }

    companion object {
        private const val TAG = "[wa]MainViewModel"
    }
}