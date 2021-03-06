package com.src.webrtc.android

import java.util.concurrent.ExecutorService

class RemoteVideoTrack(
    override val id: String,
    executorService: ExecutorService,
    videoTrack: org.webrtc.VideoTrack
) : VideoTrack() {

    override var executor: ExecutorService? = executorService

    override var internalVideoTrack: org.webrtc.VideoTrack? = videoTrack

    override fun isEnable(): Boolean {
        return internalVideoTrack?.enabled() ?: false
    }

    override fun enable(isEnable: Boolean) {
        internalVideoTrack?.let { track ->
            executor?.execute {
                track.setEnabled(isEnable)
            }
        }
    }

    override fun addRenderer(renderer: VideoRenderer) {
        if (!videoRenderers.contains(renderer)) {
            videoRenderers.add(renderer)

            internalVideoTrack?.let {
                executor?.execute {
                    it.addSink(renderer)
                }
            }
        }
    }

    override fun removeRenderer(renderer: VideoRenderer) {
        if (videoRenderers.contains(renderer)) {
            videoRenderers.remove(renderer)

            internalVideoTrack?.let {
                executor?.execute {
                    it.removeSink(renderer)
                }
            }
        }
    }

    override fun release() {
        if (videoRenderers.isNotEmpty()) {
            videoRenderers.forEach {
                internalVideoTrack?.removeSink(it)
                it.setTarget(null)
            }
            videoRenderers.clear()
        }
    }
}