package com.src.webrtc.android.sample

import android.util.Log
import com.google.firebase.firestore.*
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
    private val roomRef = firestore.collection(COLLECTION_ROOMS)
    private var roomName = ""

    private lateinit var peerJoinListener: ListenerRegistration
    private lateinit var peerLeaveListener: ListenerRegistration
    private lateinit var sdpListener: ListenerRegistration
    private lateinit var candidateListener: ListenerRegistration
    private lateinit var candidatesRemoveListener: ListenerRegistration

    private var isRoomCreatedByMe = false

    fun connectToRoom(roomName: String, self: PeerInfo) {
        Log.d(TAG, "connectToRoom $roomName $self")
        this.roomName = roomName
        roomRef.document(roomName).get(Source.SERVER)
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    registerListeners()
                    self.timestamp = System.currentTimeMillis()
                    getRoomCollection(COLLECTION_PEER_JOIN).add(self)
                } else {
                    Log.e(TAG, "No such document")
                    roomRef.document(roomName)
                        .set(RoomInfo(roomName, System.currentTimeMillis()))
                    isRoomCreatedByMe = true
                    registerListeners()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "get failed with ", exception)
                roomRef.document(roomName)
                    .set(RoomInfo(roomName, System.currentTimeMillis()))
                isRoomCreatedByMe = true
                registerListeners()
            }
    }

    fun leaveRoom(self: PeerInfo) {
        Log.d(TAG, "leaveRoom $self")
        self.timestamp = System.currentTimeMillis()
        getRoomCollection(COLLECTION_PEER_LEAVE).add(self)
        unregisterListeners()

        if (isRoomCreatedByMe) {
            closeRoom()
        }
    }

    private fun closeRoom() {
        Log.d(TAG, "closeRoom")
        getRoomCollection(COLLECTION_PEER_JOIN).get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    getRoomCollection(COLLECTION_PEER_JOIN).document(doc.id).delete()
                }
            }
        getRoomCollection(COLLECTION_PEER_LEAVE).get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    getRoomCollection(COLLECTION_PEER_LEAVE).document(doc.id).delete()
                }
            }
        getRoomCollection(COLLECTION_SDP).get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    getRoomCollection(COLLECTION_SDP).document(doc.id).delete()
                }
            }
        getRoomCollection(COLLECTION_CANDIDATE).get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    getRoomCollection(COLLECTION_CANDIDATE).document(doc.id).delete()
                }
            }
        getRoomCollection(COLLECTION_CANDIDATES_REMOVE).get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    getRoomCollection(COLLECTION_CANDIDATES_REMOVE).document(doc.id).delete()
                }
            }
        getRoomDocument().delete()
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully deleted!")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "error deleting document ", exception)
            }
    }

    fun sendSDP(sdp: Sdp) {
        getRoomCollection(COLLECTION_SDP).add(sdp)
    }

    fun sendCandidate(sdpCandidate: SdpCandidate) {
        getRoomCollection(COLLECTION_CANDIDATE).add(sdpCandidate)
    }

    fun sendCandidatesRemove(sdpCandidatesRemove: SdpCandidatesRemove) {
        getRoomCollection(COLLECTION_CANDIDATES_REMOVE).add(sdpCandidatesRemove)
    }

    private fun getRoomDocument(): DocumentReference {
        return roomRef.document(roomName)
    }

    private fun getRoomCollection(collectionId: String): CollectionReference {
        return roomRef.document(roomName).collection(collectionId)
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
        peerJoinListener = getRoomCollection(COLLECTION_PEER_JOIN).addSnapshotListener { value, e ->
            if (e != null) {
                Log.e(TAG, "listen failed.", e)
                return@addSnapshotListener
            }

            if (value!!.metadata.hasPendingWrites()) {
                return@addSnapshotListener
            }

            for (doc in value.documentChanges) {
                when (doc.type) {
                    DocumentChange.Type.ADDED -> {
                        listener.onPeerJoinReceived(doc.document.toObject(PeerInfo::class.java))
                    }
                }
            }
        }
    }

    private fun registerPeerLeaveListener() {
        peerLeaveListener =
            getRoomCollection(COLLECTION_PEER_LEAVE).addSnapshotListener { value, e ->
                if (e != null) {
                    Log.e(TAG, "listen failed.", e)
                    return@addSnapshotListener
                }

                if (value!!.metadata.hasPendingWrites()) {
                    return@addSnapshotListener
                }

                for (doc in value.documentChanges) {
                    when (doc.type) {
                        DocumentChange.Type.ADDED -> {
                            listener.onPeerLeaveReceived(doc.document.toObject(PeerInfo::class.java))
                        }
                    }
                }
            }
    }

    private fun registerSDPListener() {
        sdpListener = getRoomCollection(COLLECTION_SDP).addSnapshotListener { value, e ->
            if (e != null) {
                Log.e(TAG, "listen failed.", e)
                return@addSnapshotListener
            }

            if (value!!.metadata.hasPendingWrites()) {
                return@addSnapshotListener
            }

            for (doc in value) {
                listener.onSdpReceived(doc.toObject(Sdp::class.java))
            }
        }
    }

    private fun registerCandidateListener() {
        candidateListener =
            getRoomCollection(COLLECTION_CANDIDATE).addSnapshotListener { value, e ->
                if (e != null) {
                    Log.e(TAG, "listen failed.", e)
                    return@addSnapshotListener
                }

                if (value!!.metadata.hasPendingWrites()) {
                    return@addSnapshotListener
                }

                for (doc in value) {
                    listener.onCandidateReceived(doc.toObject(SdpCandidate::class.java))
                }
            }
    }

    private fun registerCandidatesRemoveListener() {
        candidatesRemoveListener =
            getRoomCollection(COLLECTION_CANDIDATES_REMOVE).addSnapshotListener { value, e ->
                if (e != null) {
                    Log.e(TAG, "listen failed.", e)
                    return@addSnapshotListener
                }

                if (value!!.metadata.hasPendingWrites()) {
                    return@addSnapshotListener
                }

                for (doc in value) {
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