package com.src.webrtc.android

import org.webrtc.DataChannel

class RemoteDataTrack(
    override val name: String,
    override val enable: Boolean,
    private val dataChannel: DataChannel
) : DataTrack() {

    override fun release() {
        TODO("Not yet implemented")
    }
}