package com.src.webrtc.android.sample

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.src.webrtc.android.sample.data.*

class SignalingManager(
    private val listener: SignalingListener
) {

    interface SignalingListener {

        fun onPeerJoinReceived(peerInfo: PeerInfo)

        fun onPeerLeaveReceived(peerInfo: PeerInfo)

        fun onSdpReceived(sdp: Sdp)

        fun onCandidateReceived(sdpCandidate: SdpCandidate)

        fun onCandidatesRemoveReceived(sdpCandidatesRemove: SdpCandidatesRemove)
    }

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var roomRef: DocumentReference

    private lateinit var peerJoinListener: ListenerRegistration
    private lateinit var peerLeaveListener: ListenerRegistration
    private lateinit var sdpListener: ListenerRegistration
    private lateinit var candidateListener: ListenerRegistration
    private lateinit var candidatesRemoveListener: ListenerRegistration

    fun connectToRoom(roomName: String, self: PeerInfo) {
        roomRef = firestore.collection(COLLECTION_ROOMS).document(roomName)
        roomRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    registerListeners()
                    roomRef.collection(COLLECTION_PEER_JOIN).add(self)
                } else {
                    Log.e(TAG, "No such document")
                    roomRef = firestore.collection(COLLECTION_ROOMS).document(roomName)
                    roomRef.set(RoomInfo(roomName, System.currentTimeMillis()))
                    registerListeners()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "get failed with ", exception)
                roomRef = firestore.collection(COLLECTION_ROOMS).document(roomName)
                roomRef.set(RoomInfo(roomName, System.currentTimeMillis()))
                registerListeners()
            }
    }

    fun leaveRoom(self: PeerInfo) {
        roomRef.collection(COLLECTION_PEER_LEAVE).add(self)
        unregisterListeners()
    }

    fun closeRoom() {
        roomRef.delete()
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully deleted!")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "error deleting document ", exception)
            }
    }

    fun sendSDP(sdp: Sdp) {
        roomRef.collection(COLLECTION_SDP).add(sdp)
    }

    fun sendCandidate(sdpCandidate: SdpCandidate) {
        roomRef.collection(COLLECTION_CANDIDATE).add(sdpCandidate)
    }

    fun sendCandidatesRemove(sdpCandidatesRemove: SdpCandidatesRemove) {
        roomRef.collection(COLLECTION_CANDIDATES_REMOVE).add(sdpCandidatesRemove)
    }

    private fun registerListeners() {
        registerPeerJoinListener()
        registerPeerLeaveListener()
        registerSDPListener()
        registerCandidateListener()
        registerCandidatesRemoveListener()
    }

    private fun unregisterListeners() {
        peerJoinListener.remove()
        peerLeaveListener.remove()
        sdpListener.remove()
        candidateListener.remove()
        candidatesRemoveListener.remove()
    }

    private fun registerPeerJoinListener() {
        Log.d(TAG, "registerPeerJoinListener")
        peerJoinListener =
            roomRef.collection(COLLECTION_PEER_JOIN).addSnapshotListener { value, e ->
                if (e != null) {
                    Log.e(TAG, "listen failed.", e)
                    return@addSnapshotListener
                }

                if (value!!.metadata.hasPendingWrites()) {
                    return@addSnapshotListener
                }

                for (doc in value!!) {
                    listener.onPeerJoinReceived(doc.toObject(PeerInfo::class.java))
                }
            }
    }

    private fun registerPeerLeaveListener() {
        Log.d(TAG, "registerPeerLeaveListener")
        peerLeaveListener =
            roomRef.collection(COLLECTION_PEER_LEAVE).addSnapshotListener { value, e ->
                if (e != null) {
                    Log.e(TAG, "listen failed.", e)
                    return@addSnapshotListener
                }

                if (value!!.metadata.hasPendingWrites()) {
                    return@addSnapshotListener
                }

                for (doc in value!!) {
                    listener.onPeerLeaveReceived(doc.toObject(PeerInfo::class.java))
                }
            }
    }

    private fun registerSDPListener() {
        Log.d(TAG, "registerSDPListener")
        sdpListener = roomRef.collection(COLLECTION_SDP).addSnapshotListener { value, e ->
            if (e != null) {
                Log.e(TAG, "listen failed.", e)
                return@addSnapshotListener
            }

            if (value!!.metadata.hasPendingWrites()) {
                return@addSnapshotListener
            }

            for (doc in value!!) {
                listener.onSdpReceived(doc.toObject(Sdp::class.java))
            }
        }
    }

    private fun registerCandidateListener() {
        Log.d(TAG, "registerCandidateListener")
        candidateListener =
            roomRef.collection(COLLECTION_CANDIDATE).addSnapshotListener { value, e ->
                if (e != null) {
                    Log.e(TAG, "listen failed.", e)
                    return@addSnapshotListener
                }

                if (value!!.metadata.hasPendingWrites()) {
                    return@addSnapshotListener
                }

                for (doc in value!!) {
                    listener.onCandidateReceived(doc.toObject(SdpCandidate::class.java))
                }
            }
    }

    private fun registerCandidatesRemoveListener() {
        Log.d(TAG, "registerCandidatesRemoveListener")
        candidatesRemoveListener =
            roomRef.collection(COLLECTION_CANDIDATES_REMOVE).addSnapshotListener { value, e ->
                if (e != null) {
                    Log.e(TAG, "listen failed.", e)
                    return@addSnapshotListener
                }

                if (value!!.metadata.hasPendingWrites()) {
                    return@addSnapshotListener
                }

                for (doc in value!!) {
                    listener.onCandidatesRemoveReceived(doc.toObject(SdpCandidatesRemove::class.java))
                }
            }
    }

    companion object {
        private const val TAG = "[rtc]SignalingManager"
        private const val COLLECTION_ROOMS = "rooms"
        private const val COLLECTION_PEER_JOIN = "peer_join"
        private const val COLLECTION_PEER_LEAVE = "peer_leave"
        private const val COLLECTION_SDP = "sdp"
        private const val COLLECTION_CANDIDATE = "candidate"
        private const val COLLECTION_CANDIDATES_REMOVE = "candidates_remove"
    }
}