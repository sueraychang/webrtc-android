package com.src.webrtc.android.sample

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.src.webrtc.android.*
import com.src.webrtc.android.sample.data.*
import org.webrtc.IceCandidate
import java.nio.ByteBuffer
import java.util.*

class MainViewModel(
    private val context: Application
) : AndroidViewModel(context) {

    private lateinit var self: PeerInfo

    private lateinit var signaling: SignalingManager

    lateinit var roomManager: RoomManager

    private val _room = MutableLiveData<Room>()
    val room: LiveData<Room> = _room

    private val _localPeer = MutableLiveData<LocalPeer>()
    val localPeer: LiveData<LocalPeer> = _localPeer

    private val _remotePeerEvent = MutableLiveData<Event<Pair<String, Boolean>>>()
    val remotePeerEvent: LiveData<Event<Pair<String, Boolean>>> = _remotePeerEvent

    private val _remoteVideoTrack = MutableLiveData<RemoteVideoTrack>()
    val remoteVideoTrack: LiveData<RemoteVideoTrack> = _remoteVideoTrack

    val isMicEnabled = MutableLiveData(true)
    val isCameraEnabled = MutableLiveData(true)

    fun connectToRoom(roomName: String) {
        Log.d(TAG, "createRoom")

        self = PeerInfo(UUID.randomUUID().toString(), "Harry")

        roomManager = RoomManager(context, roomListener, remotePeerListener, remoteDataListener)
        roomManager.connect(roomName, self.id, ICE_URLS)

        Log.d(TAG, "room name: $roomName")

        signaling = SignalingManager(signalingListener)
        signaling.connectToRoom(roomName, self)
    }

    fun leaveRoom() {
        roomManager.disconnect()
        signaling.leaveRoom(self)
    }

    fun toggleMic() {
        isMicEnabled.value = !(isMicEnabled.value ?: false)
        roomManager.localAudioTrack.enable(isMicEnabled.value ?: true)
    }

    fun toggleCamera() {
        isCameraEnabled.value = !(isCameraEnabled.value ?: false)
        roomManager.localVideoTrack.enable(isCameraEnabled.value ?: true)
    }

    fun switchCamera() {
        roomManager.cameraCaptureManager.switchCamera(object : Listener.CameraSwitchListener {
            override fun onCameraSwitchDone(isFrontCamera: Boolean) {
                Log.d(TAG, "onCameraSwitchDone, isFrontCamer: $isFrontCamera")
            }

            override fun onCameraSwitchError(error: String) {
                Log.e(TAG, "onCameraSwitchError: $error")
            }
        })
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
                Log.d(
                    TAG,
                    "onCandidateReceived: ${sdpCandidate.from} ${sdpCandidate.to} ${sdpCandidate.candidate}"
                )
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
        Log.d(TAG, "onCleared")

        super.onCleared()
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
            _room.value = room
            _localPeer.value = room.localPeer
        }

        override fun onConnectFailed(room: Room) {
            Log.d(TAG, "onConnectFailed")
        }

        override fun onDisconnected(room: Room) {
            Log.d(TAG, "onDisconnected")
            _room.value = null
            _localPeer.value = null
        }

        override fun onPeerConnected(room: Room, remotePeer: RemotePeer) {
            Log.d(TAG, "onPeerConnected ${remotePeer.id}")
            _remotePeerEvent.value = Event(Pair(remotePeer.id, true))
        }

        override fun onPeerDisconnected(room: Room, remotePeer: RemotePeer) {
            Log.d(TAG, "onPeerDisconnected ${remotePeer.id}")
            _remotePeerEvent.value = Event(Pair(remotePeer.id, false))
        }
    }

    private val remotePeerListener = object : Listener.RemotePeerListener {
        override fun onAudioTrackReady(remotePeer: RemotePeer, remoteAudioTrack: RemoteAudioTrack) {
            Log.d(TAG, "onAudioTrackReady ${remotePeer.id} ${remoteAudioTrack.name}")
        }

        override fun onVideoTrackReady(remotePeer: RemotePeer, remoteVideoTrack: RemoteVideoTrack) {
            Log.d(TAG, "onVideoTrackReady ${remotePeer.id} ${remoteVideoTrack.name}")
            if (remoteVideoTrack.name == "camera") {
                _remoteVideoTrack.value = remoteVideoTrack
            }
        }

        override fun onDataTrackReady(remotePeer: RemotePeer, remoteDataTrack: RemoteDataTrack) {
            Log.d(TAG, "onDataTrackReady ${remotePeer.id} ${remoteDataTrack.name}")
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