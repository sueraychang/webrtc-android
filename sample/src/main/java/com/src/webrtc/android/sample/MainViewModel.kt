package com.src.webrtc.android.sample

import android.app.Application
import android.util.Log
import android.view.View
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

    private val _peers = MutableLiveData<List<Peer>>()
    val peers: LiveData<List<Peer>> = _peers

    val isMicEnabled = MutableLiveData(true)
    val isCameraEnabled = MutableLiveData(true)

    fun connectToRoom(roomName: String) {
        Log.d(TAG, "createRoom")

        self = PeerInfo(UUID.randomUUID().toString())

        roomManager = RoomManager(context, roomListener)
        roomManager.connect(roomName, self.id, ICE_URLS)

        Log.d(TAG, "room name: $roomName")

        signaling = SignalingManager(signalingListener)
        signaling.connectToRoom(roomName, self)
    }

    fun leaveRoom() {
        _peers.value = emptyList()
        roomManager.disconnect()
        signaling.leaveRoom(self)
    }

    fun toggleMic() {
        isMicEnabled.value = !(isMicEnabled.value ?: false)
        roomManager.localAudioTrack.enable(isMicEnabled.value ?: true)
        roomManager.localDataTrack.send("toggle mic to ${isMicEnabled.value}")
    }

    fun toggleCamera() {
        isCameraEnabled.value = !(isCameraEnabled.value ?: false)
        roomManager.localVideoTrack.enable(isCameraEnabled.value ?: true)
        roomManager.localDataTrack.send("toggle camera to ${isCameraEnabled.value}")
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

    fun onSubViewClicked(view: View) {
        _peers.value?.let {
            val peers = mutableListOf<Peer>()
            peers.addAll(it)
            when (view.id) {
                R.id.sub_view1 -> {
                    if (peers.size < 2) {
                        return@let
                    } else {
                        val peer = peers.removeAt(1)
                        peers.add(0, peer)
                    }
                }
                R.id.sub_view2 -> {
                    if (peers.size < 3) {
                        return@let
                    } else {
                        val peer = peers.removeAt(2)
                        peers.add(0, peer)
                    }
                }
                R.id.sub_view3 -> {
                    if (peers.size < 4) {
                        return@let
                    } else {
                        val peer = peers.removeAt(3)
                        peers.add(0, peer)
                    }
                }
                else -> {
                }
            }
            _peers.value = peers
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

    private val roomListener = object : Listener.RoomListener {
        override fun onLocalDescription(to: String, type: SDPType, sdp: String) {
            Log.d(TAG, "onLocalDescription $to $type")
            signaling.sendSDP(Sdp(self.id, to, type.value, sdp))
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

            val peers = mutableListOf<Peer>()
            peers.add(room.localPeer!!)
            _peers.value = peers
        }

        override fun onConnectFailed(room: Room) {
            Log.d(TAG, "onConnectFailed")
        }

        override fun onDisconnected(room: Room) {
            Log.d(TAG, "onDisconnected")
            _room.value = null
        }

        override fun onPeerConnected(room: Room, remotePeer: RemotePeer) {
            Log.d(TAG, "onPeerConnected ${remotePeer.id}")
            remotePeer.registerListener(remotePeerListener)
            val peers = mutableListOf<Peer>()
            _peers.value?.let {
                peers.addAll(it)
            }
            peers.add(0, remotePeer)
            _peers.value = peers
        }

        override fun onPeerDisconnected(room: Room, remotePeer: RemotePeer) {
            Log.d(TAG, "onPeerDisconnected ${remotePeer.id}")
            val peers = mutableListOf<Peer>()
            _peers.value?.let {
                peers.addAll(it)
            }
            peers.remove(remotePeer)
            _peers.value = peers
        }
    }

    private val remotePeerListener = object : RemotePeer.Listener {
        override fun onDataTrackReady(remoteDataTrack: RemoteDataTrack) {
            remoteDataTrack.registerMessageListener(messageListener)
        }
    }

    private val messageListener = object : DataTrack.MessageListener {

        override fun onMessage(remoteDataTrack: RemoteDataTrack, byteBuffer: ByteBuffer) {

        }

        override fun onMessage(remoteDataTrack: RemoteDataTrack, message: String) {
            Log.d(TAG, "onMessage $message")
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