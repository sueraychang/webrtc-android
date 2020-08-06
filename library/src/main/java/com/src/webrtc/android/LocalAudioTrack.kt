package com.src.webrtc.android

import android.util.Log
import java.util.concurrent.ExecutorService

class LocalAudioTrack(
    override val name: String,
    val audioOptions: AudioOptions
) : AudioTrack() {

    interface LocalAudioTrackEvents {

        fun setEnable(name: String, isEnable: Boolean)
    }

    override var executor: ExecutorService? = null

    override var internalAudioTrack: org.webrtc.AudioTrack? = null

    private var events: LocalAudioTrackEvents? = null

    override fun isEnable(): Boolean {
        return internalAudioTrack?.enabled() ?: false
    }

    override fun enable(isEnable: Boolean) {
        internalAudioTrack?.let {track ->
            executor?.execute {
                track.setEnabled(isEnable)
            }
            events?.setEnable(name, isEnable)
        }
    }

    fun initInternalAudioTrack(
        internalAudioTrack: org.webrtc.AudioTrack, executorService: ExecutorService,
        events: LocalAudioTrackEvents
    ) {
        Log.d(TAG, "initInternalAudioTrack")
        this.internalAudioTrack = internalAudioTrack
        this.executor = executorService
        this.events = events
    }

    companion object {
        private const val TAG = "[rtc]LocalAudioTrack"
    }
}