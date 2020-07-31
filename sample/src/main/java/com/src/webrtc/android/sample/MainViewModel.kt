package com.src.webrtc.android.sample

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.src.webrtc.android.sample.data.Peer
import com.src.webrtc.android.sample.data.Room
import java.util.*

class MainViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var roomRef: DocumentReference

    private lateinit var peerJoin: DocumentReference

    private lateinit var room: Room

    private lateinit var self: Peer

    private var isRoomCreatedByMe = false

    fun createRoom() {
        Log.d(TAG, "createRoom")

        room = Room(UUID.randomUUID().toString().substring(0,8), System.currentTimeMillis())
        self = Peer(UUID.randomUUID().toString(), "Harry")

        Log.d(TAG, "room name: ${room.name}")

        roomRef = firestore.collection(COLLECTION_ROOMS).document(room.name)
        roomRef.set(room)

        isRoomCreatedByMe = true
//        roomRef.collection(COLLECTION_PEER_JOIN).add(self)

        registerPeerJoinListener()
        registerPeerLeaveListener()
    }

    fun joinRoom() {
        Log.d(TAG, "joinRoom")

        self = Peer(UUID.randomUUID().toString(), "Hermione")
        roomRef = firestore.collection(COLLECTION_ROOMS).document("71c7a1bb")
        roomRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    roomRef.collection(COLLECTION_PEER_JOIN).add(self)
                } else {
                    Log.e(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "get failed with ", exception)
            }

        registerPeerJoinListener()
        registerPeerLeaveListener()
    }

    fun leaveRoom() {
        roomRef.collection(COLLECTION_PEER_LEAVE).add(self)

        if (isRoomCreatedByMe) {
            roomRef.delete()
                .addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot successfully deleted!")
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "error deleting document ", exception)
                }
        }
    }

    private fun registerPeerJoinListener() {
        Log.d(TAG, "registerPeerJoinListener")
        roomRef.collection(COLLECTION_PEER_JOIN).addSnapshotListener { value, e ->
            if (e != null) {
                Log.e(TAG, "listen failed.", e)
                return@addSnapshotListener
            }

            for (doc in value!!) {
                if (doc.data["id"] != self.id) {
                    Log.d(TAG, "peer join: ${doc.data}")
                }
            }
        }
    }

    private fun registerPeerLeaveListener() {
        Log.d(TAG, "registerPeerLeaveListener")
        roomRef.collection(COLLECTION_PEER_LEAVE).addSnapshotListener { value, e ->
            if (e != null) {
                Log.e(TAG, "listen failed.", e)
                return@addSnapshotListener
            }

            for (doc in value!!) {
                if (doc.data["id"] != self.id) {
                    Log.d(TAG, "peer leave: ${doc.data}")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared")
    }

    companion object {
        private const val TAG = "[wa]MainViewModel"

        private const val COLLECTION_ROOMS = "rooms"
//        private const val COLLECTION_SIGNALING = "signaling"
        private const val COLLECTION_PEER_JOIN = "peer_join"
        private const val COLLECTION_PEER_LEAVE = "peer_leave"
    }
}