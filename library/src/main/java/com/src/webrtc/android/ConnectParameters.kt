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

    /**
     * Build new [ConnectParameters].
     *
     *
     * All methods are optional.
     */
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

        /**
         * Set the preferred audio codec.
         * Audio codec is not guaranteed to be satisfied because not all peers
         * are guaranteed to support this audio codec.
         */
        fun preferredAudioCodec(codec: AudioCodec): Builder {
            preferredAudioCodec = codec
            return this
        }

        /**
         * Set the preferred video codec.
         * Video codec is not guaranteed to be satisfied because not all peers
         * are guaranteed to support this video codec.
         */
        fun preferredVideoCodec(codec: VideoCodec): Builder {
            preferredVideoCodec = codec
            return this
        }

        /**
         * Set start audio send bitrate in bytes per second. Zero indicates the WebRTC default value, which
         * is 32 kbps.
         */
        fun startAudioBitrate(bitrate: Int): Builder {
            startAudioBitrate = bitrate
            return this
        }

        /**
         * Set maximum video send bitrate in bytes per second. Zero indicates the WebRTC default value, which
         * is 1700 kbps.
         */
        fun maxVideoBitrate(bitrate: Int): Builder {
            maxVideoBitrate = bitrate
            return this
        }

        /**
         * The audio tracks that will be use in the room.
         */
        fun audioTracks(audioTracks: List<LocalAudioTrack>): Builder {
            this.audioTracks = audioTracks
            return this
        }

        /**
         * The video tracks that will be use in the room.
         */
        fun videoTracks(videoTracks: List<LocalVideoTrack>): Builder {
            this.videoTracks = videoTracks
            return this
        }

        /**
         * The data tracks that will be use in the room.
         */
        fun dataTracks(dataTracks: List<LocalDataTrack>): Builder {
            this.dataTracks = dataTracks
            return this
        }

        /**
         * Builds [ConnectParameters] objects.
         */
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