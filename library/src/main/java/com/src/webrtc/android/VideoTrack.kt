package com.src.webrtc.android

import java.util.*
import java.util.concurrent.ExecutorService

abstract class VideoTrack : Track {

    abstract var executor: ExecutorService?

    abstract var internalVideoTrack: org.webrtc.VideoTrack?

    protected var videoRenderers = mutableListOf<VideoRenderer>()

    abstract fun isEnable() : Boolean

    abstract fun enable(isEnable: Boolean)

    abstract fun addRenderer(renderer: VideoRenderer)

    abstract fun removeRenderer(renderer: VideoRenderer)

    abstract fun release()

}