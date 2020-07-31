package com.src.webrtc.android

import java.util.concurrent.ExecutorService

class LocalDataTrack(
    override val name: String,
    override val enable: Boolean
) : DataTrack() {

    override var executor: ExecutorService? = null

    override fun enable(isEnable: Boolean) {
        TODO("Not yet implemented")
    }

    override fun release() {
        TODO("Not yet implemented")
    }
}