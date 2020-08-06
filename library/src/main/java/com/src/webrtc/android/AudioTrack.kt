package com.src.webrtc.android

import java.util.concurrent.ExecutorService

abstract class AudioTrack: Track {

    abstract var executor: ExecutorService?

    abstract var internalAudioTrack: org.webrtc.AudioTrack?

    abstract fun isEnable() : Boolean

    abstract fun enable(isEnable: Boolean)
}