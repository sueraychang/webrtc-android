package com.src.webrtc.android

import java.util.concurrent.ExecutorService

class RemoteAudioTrack(
    override val name: String
): AudioTrack() {

    override var executor: ExecutorService? = null

    override fun enable(isEnable: Boolean) {
        TODO("Not yet implemented")
    }

    override fun release() {
        TODO("Not yet implemented")
    }
}