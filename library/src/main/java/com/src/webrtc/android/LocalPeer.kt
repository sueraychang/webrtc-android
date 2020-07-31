package com.src.webrtc.android

import android.content.Context
import android.util.Log
import org.webrtc.EglBase
import org.webrtc.PeerConnection.IceServer
import org.webrtc.PeerConnectionFactory
import org.webrtc.VideoCapturer
import java.util.concurrent.ExecutorService

class LocalPeer(
    id: String,
    context: Context,
    eglBase: EglBase,
    connectionParameters: ConnectParameters,
    peerConnectionParameters: PeerConnectionParameters,
    executorService: ExecutorService
): Peer(id, context, eglBase, peerConnectionParameters, executorService) {

    private val audioTracks = mutableMapOf<String, LocalAudioTrack>()
    private val videoTracks = mutableMapOf<String, LocalVideoTrack>()
    private val dataTracks = mutableMapOf<String, LocalDataTrack>()

    private val iceServers = mutableListOf<IceServer>()

    init {
        connectionParameters.let { param ->
            param.iceUrls.forEach {
                iceServers.add(IceServer(it))
            }
            param.audioTracks.forEach {
                audioTracks[it.name] = it
            }
            param.videoTracks.forEach {
                videoTracks[it.name] = it
            }
            param.dataTracks.forEach {
                dataTracks[it.name] = it
            }
        }

        createPeerConnectionFactory(PeerConnectionFactory.Options())
    }

    override fun release() {
        Log.d(TAG, "release")

        localPeerClose()

        Log.d(TAG, "release done")
    }

    override fun createLocalAudioTracks() {
        if (audioTracks.isEmpty()) {
            return
        }

        TODO("createLocalAudioTracks")
    }

    override fun createLocalVideoTracks() {
        if (videoTracks.isEmpty()) {
            return
        }

        TODO("createLocalVideoTracks")
    }

    fun getIceServers(): List<IceServer> {
        return iceServers
    }

    fun getVideoCapturer(): VideoCapturer? {
        return videoCapturer
    }

    fun getAudioTracks(): Map<String, LocalAudioTrack> {
        return audioTracks
    }

    fun getVideoTracks(): Map<String, LocalVideoTrack> {
        return videoTracks
    }

    fun getDataTracks(): Map<String, LocalDataTrack> {
        return dataTracks
    }

    companion object {

        private const val TAG = "[rtc]LocalPeer"
    }
}