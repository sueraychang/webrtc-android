package com.src.webrtc.android

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import org.appspot.apprtc.AppRTCClient
import org.webrtc.*
import java.util.concurrent.ExecutorService

class RemotePeer(
    id: String,
    context: Context,
    isInitiator: Boolean,
    private val localPeer: LocalPeer,
    eglBase: EglBase,
    peerConnectionParameters: PeerConnectionParameters,
    executorService: ExecutorService,
    private val events: RemotePeerEvents
) : Peer(id, context, eglBase, peerConnectionParameters, executorService) {

    private val handler = Handler(Looper.getMainLooper())

    private val signalingParameters = AppRTCClient.SignalingParameters(
        localPeer.getIceServers(),
        isInitiator,
        "",
        "",
        "",
        null,
        null
    )

    private val audioTracks = mutableMapOf<String, RemoteAudioTrack>()
    private val videoTracks = mutableMapOf<String, RemoteVideoTrack>()
    private val dataTracks = mutableMapOf<String, RemoteDataTrack>()

    private val peerConnectionEvents = object: PeerConnectionEvents {
        override fun onLocalDescription(sdp: SessionDescription) {
            handler.post {
                if (signalingParameters.initiator) {
                    events.onLocalDescription(id, SIG_TYPE_OFFER, sdp.description)
                } else {
                    events.onLocalDescription(id, SIG_TYPE_ANSWER, sdp.description)
                }
            }
        }

        override fun onIceCandidate(candidate: IceCandidate) {
            events.onIceCandidate(id, candidate)
        }

        override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>) {
            events.onIceCandidatesRemoved(id, candidates)
        }

        override fun onIceConnected() {
            Log.d(TAG, "onIceConnected")
        }

        override fun onIceDisconnected() {
            Log.d(TAG, "onIceDisconnected")
        }

        override fun onConnected() {
            Log.d(TAG, "onConnected")
            events.onConnected(id)
        }

        override fun onDisconnected() {
            Log.d(TAG, "onDisconnected")
            events.onDisconnected(id)
        }

        override fun onPeerConnectionClosed() {
            Log.d(TAG, "onPeerConnectionClosed")
            events.onPeerConnectionClose(id)
        }

        override fun onPeerConnectionStatsReady(reports: Array<out StatsReport>) {
            TODO("Not yet implemented")
        }

        override fun onPeerConnectionError(description: String) {
            Log.d(TAG, "onPeerConnectionError $description")
            events.onPeerConnectionError(id, description)
        }

        override fun onAddStream(stream: MediaStream) {
            Log.d(TAG, "onAddStream $stream")
            if (stream.videoTracks.isNotEmpty()) {
                stream.videoTracks.forEach {
                    Log.d(TAG, "video stream ${stream.id}")
                    val remoteVideoTrack = RemoteVideoTrack(stream.id, executor, it)
                    videoTracks[remoteVideoTrack.name] = remoteVideoTrack
                }
            }
            if (stream.audioTracks.isNotEmpty()) {
                stream.audioTracks.forEach {
                    Log.d(TAG, "audio stream ${stream.id}")
                    val remoteAudioTrack = RemoteAudioTrack(stream.id, executor, it)
                    audioTracks[remoteAudioTrack.name] = remoteAudioTrack
                }
            }
        }

        override fun onRemoveStream(stream: MediaStream) {
            Log.d(TAG, "onRemoveStream")
        }

        override fun onDataChannel(dc: DataChannel) {
            Log.d(TAG, "onDataChannel")
            val remoteDataTrack = RemoteDataTrack(dc.label(), dc, dataTrackEvents)
            dataTracks[dc.label()] = remoteDataTrack
            events.onDataChannel(id, dc.label())
        }
    }

    private val dataTrackEvents = object: DataTrack.Events {
        override fun onMessage(label: String, buffer: DataChannel.Buffer) {
            Log.d(TAG, "onMessage $label")
            events.onMessage(id, label, buffer)
        }
    }

    init {
        copyMemberVariable(localPeer)

        setPeerConnectionEvents(peerConnectionEvents)

        createPeerConnection(null, null, localPeer.getVideoCapturer(), signalingParameters)
    }

    override fun release() {
        Log.d(TAG, "release")
        audioTracks.clear()
        videoTracks.forEach { (_, track) ->
            track.release()
        }
        videoTracks.clear()
        dataTracks.forEach { (_, track) ->
            track.release()
        }
        dataTracks.clear()
        remotePeerClose()
        Log.d(TAG, "release done")
    }

    override fun createLocalAudioTracks() {
        // Keep this empty.
    }

    override fun createLocalVideoTracks() {
        // Keep this empty.
    }

    override fun addLocalAudioTracks() {
        localPeer.addLocalAudioTracks()
    }

    override fun addLocalVideoTracks() {
        localPeer.addLocalVideoTracks()
    }

    override fun addLocalDataTracks(id: String) {
        localPeer.addLocalDataTracks(this.id)
    }

    override fun copyPeerConnection(peerConnection: PeerConnection) {
        localPeer.copyPeerConnection(peerConnection)
    }

    fun getAudioTracks(): Map<String, RemoteAudioTrack> {
        return audioTracks
    }

    fun getVideoTracks(): Map<String, RemoteVideoTrack> {
        return videoTracks
    }

    fun getDataTracks(): Map<String, RemoteDataTrack> {
        return dataTracks
    }

    fun sdpHandshake() {
        Log.d(TAG, "sdkHandshake")
        if (signalingParameters.initiator) {
            Log.i(TAG, "Creating OFFER...")
            // Create offer. Offer SDP will be sent to answering client in
            // PeerConnectionEvents.onLocalDescription event.
            createOffer()
        } else {
            if (signalingParameters.offerSdp != null) {
                setRemoteDescription(signalingParameters.offerSdp)
                Log.i(TAG, "Creating ANSWER...")
                // Create answer. Answer SDP will be sent to offering client in
                // PeerConnectionEvents.onLocalDescription event.
                createAnswer()
            }
            if (signalingParameters.iceCandidates != null) {
                // Add remote ICE candidates from room.
                for (iceCandidate in signalingParameters.iceCandidates) {
                    addRemoteIceCandidate(iceCandidate)
                }
            }
        }
    }

    fun onRemoteDescription(sdp: SessionDescription) {
        Log.d(TAG, "onRemoteDescription")

        setRemoteDescription(sdp)

        if (!signalingParameters.initiator) {
            Log.d(TAG, "Creating ANSWER...")

            createAnswer()
        }
    }

    companion object {

        private const val TAG = "[rtc]RemotePeer"
    }
}