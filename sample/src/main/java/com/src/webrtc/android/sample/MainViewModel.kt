package com.src.webrtc.android.sample

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.src.webrtc.android.*
import com.src.webrtc.android.sample.data.*
import org.webrtc.IceCandidate
import java.nio.ByteBuffer
import java.util.*

class MainViewModel(
    private val context: Application
) : AndroidViewModel(context) {

    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var roomRef: DocumentReference

    private lateinit var peerJoin: DocumentReference

    private lateinit var roomInfo: RoomInfo

    private lateinit var self: PeerInfo

    private lateinit var signaling: SignalingManager

    private lateinit var roomManager: RoomManager

    private var isRoomCreatedByMe = false

    fun createRoom() {
        Log.d(TAG, "createRoom")

        roomInfo =
            RoomInfo(UUID.randomUUID().toString().substring(0, 8), System.currentTimeMillis())
        self = PeerInfo(UUID.randomUUID().toString(), "Harry")

        roomManager = RoomManager(context, roomListener, remotePeerListener, remoteDataListener)
        roomManager.connect(roomInfo.name, self.id, ICE_URLS)

        Log.d(TAG, "room name: ${roomInfo.name}")
        isRoomCreatedByMe = true

        signaling = SignalingManager(signalingListener)
        signaling.createRoom(roomInfo)
    }

    fun joinRoom(roomName: String) {
        Log.d(TAG, "joinRoom")
        self = PeerInfo(UUID.randomUUID().toString(), "Hermione")

        roomManager = RoomManager(context, roomListener, remotePeerListener, remoteDataListener)
        roomManager.connect(roomName, self.id, ICE_URLS)

        signaling = SignalingManager(signalingListener)
        signaling.joinRoom(roomName, self)
    }

    fun leaveRoom() {
        roomManager.disconnect()
        signaling.leaveRoom(self)

        if (isRoomCreatedByMe) {
            signaling.closeRoom()
        }
    }

    private val signalingListener = object : SignalingManager.SignalingListener {
        override fun onPeerJoinReceived(peerInfo: PeerInfo) {
            if (peerInfo != self) {
                Log.d(TAG, "onPeerJoinReceived: $peerInfo")
                roomManager.onPeerJoin(peerInfo.id)
            }
        }

        override fun onPeerLeaveReceived(peerInfo: PeerInfo) {
            if (peerInfo != self) {
                Log.d(TAG, "onPeerLeaveReceived: $peerInfo")
                roomManager.onPeerLeave(peerInfo.id)
            }
        }

        override fun onSdpReceived(sdp: Sdp) {
            if (sdp.to == self.id) {
                Log.d(TAG, "onSdpReceived: ${sdp.from} ${sdp.to} ${sdp.type}")
                roomManager.onSdpReceived(sdp)
            }
        }

        override fun onCandidateReceived(sdpCandidate: SdpCandidate) {
            if (sdpCandidate.to == self.id) {
                Log.d(TAG, "onCandidateReceived: ${sdpCandidate.from} ${sdpCandidate.to} ${sdpCandidate.candidate}")
                roomManager.onCandidateReceived(sdpCandidate)
            }
        }

        override fun onCandidatesRemoveReceived(sdpCandidatesRemove: SdpCandidatesRemove) {
            if (sdpCandidatesRemove.to == self.id) {
                Log.d(
                    TAG,
                    "onCandidatesRemoveReceived: ${sdpCandidatesRemove.from} ${sdpCandidatesRemove.to} ${sdpCandidatesRemove.candidates}"
                )
                roomManager.onCandidatesRemoveReceived(sdpCandidatesRemove)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared")
    }

    private val roomListener = object : Listener.RoomListener {
        override fun onLocalDescription(to: String, type: String, sdp: String) {
            Log.d(TAG, "onLocalDescription $to $type")
            signaling.sendSDP(Sdp(self.id, to, type, sdp))
        }

        override fun onIceCandidate(to: String, iceCandidate: IceCandidate) {
            Log.d(TAG, "onIceCandidate $to $iceCandidate")
            signaling.sendCandidate(
                SdpCandidate(
                    self.id,
                    to,
                    Candidate(iceCandidate.sdpMid, iceCandidate.sdpMLineIndex, iceCandidate.sdp)
                )
            )
        }

        override fun onIceCandidatesRemove(to: String, iceCandidates: Array<out IceCandidate>) {
            Log.d(TAG, "onIceCandidatesRemove $to $iceCandidates")
            val candidates = mutableListOf<Candidate>()
            iceCandidates.forEach {
                candidates.add(Candidate(it.sdpMid, it.sdpMLineIndex, it.sdp))
            }
            signaling.sendCandidatesRemove(SdpCandidatesRemove(self.id, to, candidates))
        }

        override fun onConnected(room: Room) {
            Log.d(TAG, "onConnected")
        }

        override fun onConnectFailed(room: Room) {
            Log.d(TAG, "onConnectFailed")
        }

        override fun onDisconnected(room: Room) {
            Log.d(TAG, "onDisconnected")
        }

        override fun onPeerConnected(room: Room, remotePeer: RemotePeer) {
            Log.d(TAG, "onPeerConnected ${remotePeer.id}")
        }

        override fun onPeerDisconnected(room: Room, remotePeer: RemotePeer) {
            Log.d(TAG, "onPeerDisconnected ${remotePeer.id}")
        }
    }

    private val remotePeerListener = object : Listener.RemotePeerListener {
        override fun onAudioTrackEnabled(
            remotePeer: RemotePeer,
            remoteAudioTrack: RemoteAudioTrack
        ) {
            TODO("Not yet implemented")
        }

        override fun onAudioTrackDisabled(
            remotePeer: RemotePeer,
            remoteAudioTrack: RemoteAudioTrack
        ) {
            TODO("Not yet implemented")
        }

        override fun onVideoTrackEnabled(
            remotePeer: RemotePeer,
            remoteVideoTrack: RemoteVideoTrack
        ) {
            TODO("Not yet implemented")
        }

        override fun onVideoTrackDisabled(
            remotePeer: RemotePeer,
            remoteVideoTrack: RemoteVideoTrack
        ) {
            TODO("Not yet implemented")
        }

        override fun onDataTrackEnabled(remotePeer: RemotePeer, remoteDataTrack: RemoteDataTrack) {
            TODO("Not yet implemented")
        }

        override fun onDataTrackDisabled(remotePeer: RemotePeer, remoteDataTrack: RemoteDataTrack) {
            TODO("Not yet implemented")
        }
    }

    private val remoteDataListener = object : Listener.RemoteDataListener {
        override fun onMessage(
            remotePeer: RemotePeer,
            remoteDataTrack: RemoteDataTrack,
            byteBuffer: ByteBuffer
        ) {
            TODO("Not yet implemented")
        }

        override fun onMessage(
            remotePeer: RemotePeer,
            remoteDataTrack: RemoteDataTrack,
            message: String
        ) {
            TODO("Not yet implemented")
        }
    }

    companion object {
        private const val TAG = "[rtc]MainViewModel"

        private val ICE_URLS = listOf(
            "stun:stun1.l.google.com:19302",
            "stun:stun2.l.google.com:19302"
        )
    }
}