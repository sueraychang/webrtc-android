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
    private var roomName = ""
//    private lateinit var roomRef: DocumentReference

    private lateinit var peerJoinListener: ListenerRegistration
    private lateinit var peerLeaveListener: ListenerRegistration
    private lateinit var sdpListener: ListenerRegistration
    private lateinit var candidateListener: ListenerRegistration
    private lateinit var candidatesRemoveListener: ListenerRegistration

    private var isRoomCreatedByMe = false

    fun connectToRoom(roomName: String, self: PeerInfo) {
        Log.d(TAG, "connectToRoom $roomName $self")
        this.roomName = roomName
//        roomRef = firestore.collection(COLLECTION_ROOMS).document(roomName)
        firestore.collection(COLLECTION_ROOMS).document(roomName).get(Source.SERVER)
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    registerListeners()
                    self.timestamp = System.currentTimeMillis()
//                    roomRef.collection(COLLECTION_PEER_JOIN).add(self)
                    getRoomRef().collection(COLLECTION_PEER_JOIN).add(self)
                } else {
                    Log.e(TAG, "No such document")
//                    roomRef = firestore.collection(COLLECTION_ROOMS).document(roomName)
//                    roomRef.set(RoomInfo(roomName, System.currentTimeMillis()))
                    firestore.collection(COLLECTION_ROOMS).document(roomName)
                        .set(RoomInfo(roomName, System.currentTimeMillis()))
                    isRoomCreatedByMe = true
                    registerListeners()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "get failed with ", exception)
//                roomRef = firestore.collection(COLLECTION_ROOMS).document(roomName)
//                roomRef.set(RoomInfo(roomName, System.currentTimeMillis()))
                firestore.collection(COLLECTION_ROOMS).document(roomName)
                    .set(RoomInfo(roomName, System.currentTimeMillis()))
                isRoomCreatedByMe = true
                registerListeners()
            }
    }

    fun leaveRoom(self: PeerInfo) {
        Log.d(TAG, "leaveRoom $self")
        self.timestamp = System.currentTimeMillis()
        getRoomRef().collection(COLLECTION_PEER_LEAVE).add(self)
//        roomRef.collection(COLLECTION_PEER_LEAVE).add(self)
        unregisterListeners()

        if (isRoomCreatedByMe) {
            closeRoom()
        }
    }

    private fun closeRoom() {
        Log.d(TAG, "closeRoom")
        getRoomRef().collection(COLLECTION_PEER_JOIN).get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    getRoomRef().collection(COLLECTION_PEER_JOIN).document(doc.id).delete()
                }
            }
        getRoomRef().collection(COLLECTION_PEER_LEAVE).get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    getRoomRef().collection(COLLECTION_PEER_LEAVE).document(doc.id).delete()
                }
            }
        getRoomRef().collection(COLLECTION_SDP).get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    getRoomRef().collection(COLLECTION_SDP).document(doc.id).delete()
                }
            }
        getRoomRef().collection(COLLECTION_CANDIDATE).get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    getRoomRef().collection(COLLECTION_CANDIDATE).document(doc.id).delete()
                }
            }
        getRoomRef().collection(COLLECTION_CANDIDATES_REMOVE).get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    getRoomRef().collection(COLLECTION_CANDIDATES_REMOVE).document(doc.id).delete()
                }
            }
        getRoomRef().delete()
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully deleted!")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "error deleting document ", exception)
            }
    }

    fun sendSDP(sdp: Sdp) {
        getRoomRef().collection(COLLECTION_SDP).add(sdp)
    }

    fun sendCandidate(sdpCandidate: SdpCandidate) {
        getRoomRef().collection(COLLECTION_CANDIDATE).add(sdpCandidate)
    }

    fun sendCandidatesRemove(sdpCandidatesRemove: SdpCandidatesRemove) {
        getRoomRef().collection(COLLECTION_CANDIDATES_REMOVE).add(sdpCandidatesRemove)
    }

    private fun getRoomRef(): DocumentReference {
        return firestore.collection(COLLECTION_ROOMS).document(roomName)
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
            getRoomRef().collection(COLLECTION_PEER_JOIN).addSnapshotListener { value, e ->
                if (e != null) {
                    Log.e(TAG, "listen failed.", e)
                    return@addSnapshotListener
                }

                if (value!!.metadata.hasPendingWrites()) {
                    return@addSnapshotListener
                }

                for (doc in value!!.documentChanges) {
                    when (doc.type) {
                        DocumentChange.Type.ADDED -> {
                            listener.onPeerJoinReceived(doc.document.toObject(PeerInfo::class.java))
                        }
                    }
                }
            }
    }

    private fun registerPeerLeaveListener() {
        Log.d(TAG, "registerPeerLeaveListener")
        peerLeaveListener =
            getRoomRef().collection(COLLECTION_PEER_LEAVE).addSnapshotListener { value, e ->
                if (e != null) {
                    Log.e(TAG, "listen failed.", e)
                    return@addSnapshotListener
                }

                if (value!!.metadata.hasPendingWrites()) {
                    return@addSnapshotListener
                }

                for (doc in value!!.documentChanges) {
                    when (doc.type) {
                        DocumentChange.Type.ADDED -> {
                            listener.onPeerLeaveReceived(doc.document.toObject(PeerInfo::class.java))
                        }
                    }
                }
            }
    }

    private fun registerSDPListener() {
        Log.d(TAG, "registerSDPListener")
        sdpListener = getRoomRef().collection(COLLECTION_SDP).addSnapshotListener { value, e ->
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
            getRoomRef().collection(COLLECTION_CANDIDATE).addSnapshotListener { value, e ->
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
            getRoomRef().collection(COLLECTION_CANDIDATES_REMOVE).addSnapshotListener { value, e ->
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