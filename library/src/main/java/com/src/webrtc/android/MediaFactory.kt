package com.src.webrtc.android

import android.content.Context
import android.util.Log
import org.appspot.apprtc.PeerConnectionClient
import org.webrtc.*
import org.webrtc.audio.AudioDeviceModule
import org.webrtc.audio.JavaAudioDeviceModule.*

class MediaFactory private constructor(
    private val appContext: Context
) {

    private val eglBase = EglBaseProvider.getEglBase(this)

    private val executor = ExecutorProvider.get()

    private lateinit var factory: PeerConnectionFactory

    init {
        Log.d(TAG, "init")
        val fieldTrials = getFieldTrials()
        executor.execute {
            Log.d(TAG, "Initialize WebRTC. Field trials: $fieldTrials")
            PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(appContext)
                    .setFieldTrials(fieldTrials)
                    .setEnableInternalTracer(true)
                    .createInitializationOptions()
            )

            val adm = createJavaAudioDevice()

            val options = PeerConnectionFactory.Options()
            Log.d(TAG, "Factory networkIgnoreMask option: " + options.networkIgnoreMask)

            val encoderFactory = DefaultVideoEncoderFactory(
                eglBase.eglBaseContext,
                true /* enableIntelVp8Encoder */,
                false /* enableH254HighProfile */
            )
            val decoderFactory = DefaultVideoDecoderFactory(eglBase.eglBaseContext)

            factory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setAudioDeviceModule(adm)
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory()
            Log.d(TAG, "Peer connection factory created.")
            adm?.release()
        }
    }

    fun createVideoTrack(
        context: Context,
        name: String,
        constraints: VideoConstraints,
        videoCapturer: VideoCapturer,
        result: (track: org.webrtc.VideoTrack) -> Unit
    ) {
        executor.execute {
            val surfaceTextureHelper =
                SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext)
            val videoSource = factory.createVideoSource(videoCapturer.isScreencast)
            videoCapturer.initialize(
                surfaceTextureHelper,
                context,
                videoSource!!.capturerObserver
            )
            val width = constraints.resolution.width
            val height = constraints.resolution.height
            val fps = constraints.fps
            videoCapturer.startCapture(width, height, fps)
            result(factory.createVideoTrack(name, videoSource))
        }
    }

    fun createAudioTrack(
        name: String,
        audioOptions: AudioOptions,
        result: (track: org.webrtc.AudioTrack) -> Unit
    ) {
        executor.execute {
            val constraints = MediaConstraints()

            if (!audioOptions.echoCancellation) {
                constraints.mandatory.add(MediaConstraints.KeyValuePair(AUDIO_ECHO_CANCELLATION_CONSTRAINT, "false"))
            }
            if (!audioOptions.autoGainControl) {
                constraints.mandatory.add(MediaConstraints.KeyValuePair(AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "false"))
            }
            if (!audioOptions.highPassFilter) {
                constraints.mandatory.add(MediaConstraints.KeyValuePair(AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "false"))
            }
            if (!audioOptions.noiseSuppression) {
                constraints.mandatory.add(MediaConstraints.KeyValuePair(AUDIO_NOISE_SUPPRESSION_CONSTRAINT, "false"))
            }
            val audioSource = factory.createAudioSource(constraints)
            result(factory.createAudioTrack(name, audioSource))
        }
    }

    fun createPeerConnection(rtcConfig: PeerConnection.RTCConfiguration, pcObserver: PeerConnectionClient.PCObserver): PeerConnection? {
        return factory.createPeerConnection(rtcConfig, pcObserver)
    }

    fun release() {
        Log.d(TAG, "release")

        factory.dispose()

        PeerConnectionFactory.stopInternalTracingCapture()
        PeerConnectionFactory.shutdownInternalTracer()

        EglBaseProvider.release(this)
        INSTANCE = null
    }

    private fun createJavaAudioDevice(): AudioDeviceModule? {
        // Enable/disable OpenSL ES playback.
//        if (!peerConnectionParameters.useOpenSLES) {
//            Log.w(
//                PeerConnectionClient.TAG,
//                "External OpenSLES ADM not implemented yet."
//            )
//            // TODO(magjed): Add support for external OpenSLES ADM.
//        }

        // Set audio record error callbacks.
        val audioRecordErrorCallback: AudioRecordErrorCallback = object : AudioRecordErrorCallback {
            override fun onWebRtcAudioRecordInitError(errorMessage: String) {
                Log.e(TAG, "onWebRtcAudioRecordInitError: $errorMessage")
//                reportError(errorMessage)
            }

            override fun onWebRtcAudioRecordStartError(
                errorCode: AudioRecordStartErrorCode,
                errorMessage: String
            ) {
                Log.e(TAG, "onWebRtcAudioRecordStartError: $errorCode. $errorMessage")
//                reportError(errorMessage)
            }

            override fun onWebRtcAudioRecordError(errorMessage: String) {
                Log.e(TAG, "onWebRtcAudioRecordError: $errorMessage")
//                reportError(errorMessage)
            }
        }
        val audioTrackErrorCallback: AudioTrackErrorCallback = object : AudioTrackErrorCallback {
            override fun onWebRtcAudioTrackInitError(errorMessage: String) {
                Log.e(TAG, "onWebRtcAudioTrackInitError: $errorMessage")
//                reportError(errorMessage)
            }

            override fun onWebRtcAudioTrackStartError(
                errorCode: AudioTrackStartErrorCode,
                errorMessage: String
            ) {
                Log.e(TAG, "onWebRtcAudioTrackStartError: $errorCode. $errorMessage")
//                reportError(errorMessage)
            }

            override fun onWebRtcAudioTrackError(errorMessage: String) {
                Log.e(TAG, "onWebRtcAudioTrackError: $errorMessage")
//                reportError(errorMessage)
            }
        }

        // Set audio record state callbacks.
        val audioRecordStateCallback: AudioRecordStateCallback = object : AudioRecordStateCallback {
            override fun onWebRtcAudioRecordStart() {
                Log.i(TAG, "Audio recording starts")
            }

            override fun onWebRtcAudioRecordStop() {
                Log.i(TAG, "Audio recording stops")
            }
        }

        // Set audio track state callbacks.
        val audioTrackStateCallback: AudioTrackStateCallback = object : AudioTrackStateCallback {
            override fun onWebRtcAudioTrackStart() {
                Log.i(TAG, "Audio playout starts")
            }

            override fun onWebRtcAudioTrackStop() {
                Log.i(TAG, "Audio playout stops")
            }
        }
        return builder(appContext)
//            .setSamplesReadyCallback(saveRecordedAudioToFile)
            .setUseHardwareAcousticEchoCanceler(true)
            .setUseHardwareNoiseSuppressor(true)
            .setAudioRecordErrorCallback(audioRecordErrorCallback)
            .setAudioTrackErrorCallback(audioTrackErrorCallback)
            .setAudioRecordStateCallback(audioRecordStateCallback)
            .setAudioTrackStateCallback(audioTrackStateCallback)
            .createAudioDeviceModule()
    }

    companion object {

        private const val TAG = "[rtc]MediaFactory"

        private const val AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation"
        private const val AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT = "googAutoGainControl"
        private const val AUDIO_HIGH_PASS_FILTER_CONSTRAINT = "googHighpassFilter"
        private const val AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression"

        private var INSTANCE: MediaFactory? = null

        @JvmStatic
        fun get(context: Context): MediaFactory {
            return INSTANCE ?: MediaFactory(context).apply { INSTANCE = this }
        }

        private const val VIDEO_VP8_INTEL_HW_ENCODER_FIELDTRIAL = "WebRTC-IntelVP8/Enabled/"
        private fun getFieldTrials(/*peerConnectionParameters: PeerConnectionParameters*/): String {
            var fieldTrials = ""
//            if (peerConnectionParameters.videoFlexfecEnabled) {
//                fieldTrials += PeerConnectionClient.VIDEO_FLEXFEC_FIELDTRIAL
//                Log.d(PeerConnectionClient.TAG, "Enable FlexFEC field trial.")
//            }
            fieldTrials += VIDEO_VP8_INTEL_HW_ENCODER_FIELDTRIAL
//            if (peerConnectionParameters.disableWebRtcAGCAndHPF) {
//                fieldTrials += PeerConnectionClient.DISABLE_WEBRTC_AGC_FIELDTRIAL
//                Log.d(PeerConnectionClient.TAG, "Disable WebRTC AGC field trial.")
//            }
            return fieldTrials
        }
    }
}