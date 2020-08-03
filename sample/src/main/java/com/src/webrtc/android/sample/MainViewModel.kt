package com.src.webrtc.android.sample

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.src.webrtc.android.*
import com.src.webrtc.android.sample.data.*
import org.webrtc.IceCandidate
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
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

    private lateinit var localVideoTrack: LocalVideoTrack



//    private lateinit var mainView: SurfaceViewRenderer
//    private val mainViewRenderer = VideoRenderer()
//
//    private lateinit var subView: SurfaceViewRenderer
//    private val subViewRenderer = VideoRenderer()

    private var isRoomCreatedByMe = false

    fun connectToRoom(roomName: String) {
        Log.d(TAG, "createRoom")

        self = PeerInfo(UUID.randomUUID().toString(), "Harry")

        roomManager = RoomManager(context, roomListener, remotePeerListener, remoteDataListener)
        roomManager.connect(roomName, self.id, ICE_URLS)

        Log.d(TAG, "room name: $roomName")
        isRoomCreatedByMe = true

        signaling = SignalingManager(signalingListener)
        signaling.connectToRoom(roomName, self)
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

//            mainView.init(room.eglBase.eglBaseContext, null)
//            mainView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
//            mainView.setEnableHardwareScaler(false)
//            mainView.setMirror(true)
            _room.value = room
            _localPeer.value = room.localPeer
//            localVideoTrack = room.localPeer.getVideoTracks()["camera"] ?: error("Camera track not found.")
//            localVideoTrack.addRenderer(mainViewRenderer)
        }

        override fun onConnectFailed(room: Room) {
            Log.d(TAG, "onConnectFailed")
        }

        override fun onDisconnected(room: Room) {
            Log.d(TAG, "onDisconnected")

//            subView.clearImage()
//            mainView.clearImage()
//            subView.release()
//            mainView.release()
            _room.value = null
            _localPeer.value = null
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
            Log.d(TAG, "onVideoTrackEnabled ${remotePeer.id} ${remoteVideoTrack.name}")
//            subView.init(roomManager.room.eglBase.eglBaseContext, null)
//            subView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
//            subView.setZOrderMediaOverlay(true);
//            subView.setEnableHardwareScaler(true)
//
//            remoteVideoTrack.addRenderer(subViewRenderer)
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