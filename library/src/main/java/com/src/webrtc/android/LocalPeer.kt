package com.src.webrtc.android

import android.content.Context
import android.util.Log
import org.webrtc.*
import org.webrtc.PeerConnection.IceServer
import java.util.concurrent.ExecutorService

class LocalPeer(
    id: String,
    context: Context,
    eglBase: EglBase,
    connectionParameters: ConnectParameters,
    peerConnectionParameters: PeerConnectionParameters,
    executorService: ExecutorService,
    events: Events
) : Peer(id, context, eglBase, peerConnectionParameters, executorService) {

    interface Events {

        fun setAudioEnable(id: String, name: String, isEnabled: Boolean)

        fun setVideoEnable(id: String, name: String, isEnabled: Boolean)
    }

    private val audioTracks = mutableMapOf<String, LocalAudioTrack>()
    private val videoTracks = mutableMapOf<String, LocalVideoTrack>()
    private val dataTracks = mutableMapOf<String, LocalDataTrack>()

    private val iceServers = mutableListOf<IceServer>()

    init {
        Log.d(TAG, "init")
        connectionParameters.let { param ->
            param.iceUrls.forEach {
                iceServers.add(IceServer(it))
            }
            if (param.audioTracks.isNotEmpty()) {
                isAudioTrackEnabled = true
                param.audioTracks.forEach {
                    audioTracks[it.name] = it
                }
            }
            if (param.videoTracks.isNotEmpty()) {
                isVideoTrackEnabled = true
                param.videoTracks.forEach {
                    videoTracks[it.name] = it
                }
            }
            if (param.dataTracks.isNotEmpty()) {
                isDataTrackEnabled = true
                param.dataTracks.forEach {
                    dataTracks[it.name] = it
                }
            }
        }

        createPeerConnectionFactory(PeerConnectionFactory.Options())
    }

    private val localAudioTrackEvents = object: LocalAudioTrack.LocalAudioTrackEvents {
        override fun setEnable(name: String, isEnable: Boolean) {
            events.setAudioEnable(id, name, isEnable)
        }
    }

    private val localVideoTrackEvents = object: LocalVideoTrack.LocalVideoTrackEvents {
        override fun setEnable(name: String, isEnable: Boolean) {
            events.setVideoEnable(id, name, isEnable)
        }
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
        localPeerClose()
        Log.d(TAG, "release done")
    }

    override fun createLocalAudioTracks() {
        if (audioTracks.isEmpty()) {
            return
        }

        for ((name, track) in audioTracks) {
            val constraints = MediaConstraints()

            if (!track.audioOptions.echoCancellation) {
                constraints.mandatory.add(MediaConstraints.KeyValuePair(AUDIO_ECHO_CANCELLATION_CONSTRAINT, "false"))
            }
            if (!track.audioOptions.autoGainControl) {
                constraints.mandatory.add(MediaConstraints.KeyValuePair(AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "false"))
            }
            if (!track.audioOptions.highPassFilter) {
                constraints.mandatory.add(MediaConstraints.KeyValuePair(AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "false"))
            }
            if (!track.audioOptions.noiseSuppression) {
                constraints.mandatory.add(MediaConstraints.KeyValuePair(AUDIO_NOISE_SUPPRESSION_CONSTRAINT, "false"))
            }
            audioSource = factory!!.createAudioSource(constraints)
            val internalTrack = factory!!.createAudioTrack(name, audioSource)
            track.initInternalAudioTrack(internalTrack, executor, localAudioTrackEvents)
        }
    }

    override fun createLocalVideoTracks() {
        Log.d(TAG, "createLocalVideoTracks")
        if (videoTracks.isEmpty()) {
            return
        }

        for ((name, track) in videoTracks) {
            surfaceTextureHelper =
                SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext)
            videoSource = factory!!.createVideoSource(track.videoCapturer.isScreencast)
            track.videoCapturer.initialize(
                surfaceTextureHelper,
                context,
                videoSource!!.capturerObserver
            )
            val width = track.videoConstraints.resolution.width
            val height = track.videoConstraints.resolution.height
            val fps = track.videoConstraints.fps
            track.videoCapturer.startCapture(width, height, fps)
            val internalTrack = factory!!.createVideoTrack(name, videoSource)
            track.initInternalVideoTrack(
                internalTrack,
                executor,
                localVideoTrackEvents,
                surfaceTextureHelper!!
            )
        }
    }

    override fun addLocalAudioTracks() {
        Log.d(TAG, "addLocalAudioTracks")
        audioTracks.forEach { (name, track) ->
            peerConnection!!.addTrack(track.internalAudioTrack, listOf(name))
        }
    }

    override fun addLocalVideoTracks() {
        Log.d(TAG, "addLocalVideoTracks")
        videoTracks.forEach { (name, track) ->
            peerConnection!!.addTrack(track.internalVideoTrack, listOf(name))
        }
    }

    override fun addLocalDataTracks(id: String) {
        Log.d(TAG, "addLocalDataTracks")
        dataTracks.forEach { (name, track) ->
            val init = DataChannel.Init()
            init.ordered = track.options.ordered
            init.maxRetransmitTimeMs = track.options.maxPacketLifeTime
            init.maxRetransmits = track.options.maxRetransmits
            track.initInternalDataChannel(id, peerConnection!!.createDataChannel(name, init))
        }
    }

    override fun copyPeerConnection(peerConnection: PeerConnection) {
        Log.d(TAG, "copyPeerConnection")
        this.peerConnection = peerConnection
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