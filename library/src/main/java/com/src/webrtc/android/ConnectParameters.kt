package com.src.webrtc.android

import java.lang.IllegalArgumentException

class ConnectParameters {

    val roomName: String
    val localPeerId: String
    val iceUrls: List<String>
    val preferredAudioCodec: AudioCodec
    val preferredVideoCodec: VideoCodec
    val maxVideoBitrate: Int
    val startAudioBitrate: Int
    val audioTracks: List<LocalAudioTrack>
    val videoTracks: List<LocalVideoTrack>
    val dataTracks: List<LocalDataTrack>

    private constructor(builder: Builder) {
        roomName = builder.roomName
        localPeerId = builder.localPeerId
        iceUrls = builder.iceUrls
        preferredAudioCodec = builder.preferredAudioCodec
        preferredVideoCodec = builder.preferredVideoCodec
        maxVideoBitrate = builder.maxVideoBitrate
        startAudioBitrate = builder.startAudioBitrate
        audioTracks = builder.audioTracks
        videoTracks = builder.videoTracks
        dataTracks = builder.dataTracks
    }

    class Builder(
        val roomName: String,
        val localPeerId: String,
        val iceUrls: List<String>
    ) {
        var preferredAudioCodec: AudioCodec = AudioCodec.OPUS
            private set
        var preferredVideoCodec: VideoCodec = VideoCodec.VP8
            private set
        var maxVideoBitrate = 0
            private set
        var startAudioBitrate = 0
            private set
        var audioTracks: List<LocalAudioTrack> = emptyList()
            private set
        var videoTracks: List<LocalVideoTrack> = emptyList()
            private set
        var dataTracks: List<LocalDataTrack> = emptyList()
            private set

        fun preferredAudioCodec(codec: AudioCodec): Builder {
            preferredAudioCodec = codec
            return this
        }

        fun preferredVideoCodec(codec: VideoCodec): Builder {
            preferredVideoCodec = codec
            return this
        }

        fun startAudioBitrate(bitrate: Int): Builder {
            startAudioBitrate = bitrate
            return this
        }

        fun maxVideoBitrate(bitrate: Int): Builder {
            maxVideoBitrate = bitrate
            return this
        }

        fun audioTracks(audioTracks: List<LocalAudioTrack>): Builder {
            this.audioTracks = audioTracks
            return this
        }

        fun videoTracks(videoTracks: List<LocalVideoTrack>): Builder {
            this.videoTracks = videoTracks
            return this
        }

        fun dataTracks(dataTracks: List<LocalDataTrack>): Builder {
            this.dataTracks = dataTracks
            return this
        }

        fun build(): ConnectParameters {
            if (roomName.isEmpty()) {
                throw IllegalArgumentException("Room name must not be null or empty.")
            }
            if (localPeerId.isEmpty()) {
                throw IllegalArgumentException("The id of local peer is invalid.")
            }
            if (iceUrls.isNullOrEmpty()) {
                throw IllegalArgumentException("Ice must not be empty.")
            }
            return ConnectParameters(this)
        }
    }
}