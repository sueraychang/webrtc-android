package com.src.webrtc.android

import java.util.concurrent.ExecutorService

class RemoteAudioTrack(
    override val name: String,
    executorService: ExecutorService,
    audioTrack: org.webrtc.AudioTrack
): AudioTrack() {

    override var executor: ExecutorService? = executorService

    override var internalAudioTrack: org.webrtc.AudioTrack? = audioTrack

    override fun isEnable(): Boolean {
        return internalAudioTrack?.enabled() ?: false
    }

    override fun enable(isEnable: Boolean) {
        internalAudioTrack?.let {
            executor?.execute {
                it.setEnabled(isEnable)
            }
        }
    }
}