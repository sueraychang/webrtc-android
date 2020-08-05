package com.src.webrtc.android.sample

import android.content.Context
import android.util.Log
import com.src.webrtc.android.*
import com.src.webrtc.android.sample.data.Sdp
import com.src.webrtc.android.sample.data.SdpCandidate
import com.src.webrtc.android.sample.data.SdpCandidatesRemove
import org.webrtc.IceCandidate


class RoomManager(
    private val context: Context,
    private val roomListener: Listener.RoomListener,
    private val remotePeerListener: Listener.RemotePeerListener,
    private val remoteDataListener: Listener.RemoteDataListener
) {

    lateinit var room: Room

    lateinit var cameraCaptureManager: CameraCaptureManager
    lateinit var localVideoTrack: LocalVideoTrack
    lateinit var localDataTrack: LocalDataTrack

    fun connect(roomName: String, selfId: String, iceUrls: List<String>) {

        val videoConstraints = VideoConstraints.Builder()
            .fps(30)
            .resolution(Resolution.HD)
            .build()
        cameraCaptureManager = CameraCaptureManager(this.context)
        localVideoTrack = LocalVideoTrack(
            "camera",
            true,
            videoConstraints,
            cameraCaptureManager.videoCapturer
        )
        localDataTrack = LocalDataTrack("data", true, DataTrackOptions.Builder().build())

        val connectParameters = ConnectParameters.Builder(roomName, selfId, iceUrls)
            .videoTracks(listOf(localVideoTrack))
            .dataTracks(listOf(localDataTrack))
            .build()

        room = Room.connect(
            context,
            connectParameters,
            roomListener,
            remotePeerListener,
            remoteDataListener
        )
    }

    fun onPeerJoin(peerId: String) {
        Log.d(TAG, "onPeerJoin: $peerId")
        room.onPeerJoin(peerId)
    }

    fun onPeerLeave(peerId: String) {
        Log.d(TAG, "onPeerLeave: $peerId")
        room.onPeerLeave(peerId)
    }

    fun onSdpReceived(sdp: Sdp) {
        Log.d(TAG, "onSdpReceived")
        room.onSdpReceived(sdp.from, sdp.type, sdp.sdp)
    }

    fun onCandidateReceived(sdpCandidate: SdpCandidate) {
        Log.d(TAG, "onCandidateReceived")
        val candidate = IceCandidate(
            sdpCandidate.candidate.sdpMid,
            sdpCandidate.candidate.sdpMLineIndex,
            sdpCandidate.candidate.candidate
        )
        room.onCandidateReceived(sdpCandidate.from, candidate)
    }

    fun onCandidatesRemoveReceived(sdpCandidatesRemove: SdpCandidatesRemove) {
        Log.d(TAG, "onCandidatesRemoveReceived")
        val candidates = mutableListOf<IceCandidate>()
        sdpCandidatesRemove.candidates.forEach {
            candidates.add(IceCandidate(it.sdpMid, it.sdpMLineIndex, it.candidate))
        }
        room.onCandidatesRemoveReceived(sdpCandidatesRemove.from, candidates.toTypedArray())
    }

    fun disconnect() {
        Log.d(TAG, "disconnect")
        room.disconnect()
    }

    companion object {
        private const val TAG = "[rtc]RoomManager"
    }
}