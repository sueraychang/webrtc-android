package com.src.webrtc.android

import java.util.concurrent.ExecutorService

abstract class VideoTrack : Track {

    abstract var executor: ExecutorService?

    abstract fun enable(isEnable: Boolean)

    abstract fun release()

}