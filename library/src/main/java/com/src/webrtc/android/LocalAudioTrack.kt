package com.src.webrtc.android

import android.content.Context
import android.util.Log
import java.util.concurrent.ExecutorService

class LocalAudioTrack(
    context: Context,
    override val id: String,
    audioOptions: AudioOptions
) : AudioTrack() {

    override var executor: ExecutorService? = null

    override var internalAudioTrack: org.webrtc.AudioTrack? = null

    override fun isEnable(): Boolean {
        return internalAudioTrack?.enabled() ?: false
    }

    override fun enable(isEnable: Boolean) {
        internalAudioTrack?.let {track ->
            executor?.execute {
                track.setEnabled(isEnable)
            }
        }
    }

    init {
        MediaFactory.get(context).createAudioTrack(id, audioOptions) {
            internalAudioTrack = it
        }
    }

//    fun initInternalAudioTrack(
//        internalAudioTrack: org.webrtc.AudioTrack, executorService: ExecutorService
//    ) {
//        Log.d(TAG, "initInternalAudioTrack")
//        this.internalAudioTrack = internalAudioTrack
//        this.executor = executorService
//    }

    companion object {
        private const val TAG = "[rtc]LocalAudioTrack"
    }
}