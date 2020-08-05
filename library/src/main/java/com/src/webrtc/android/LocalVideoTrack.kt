package com.src.webrtc.android

import android.util.Log
import org.webrtc.RtpSender
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import java.util.concurrent.ExecutorService

class LocalVideoTrack(
    override val name: String,
    override val enable: Boolean,
    val videoConstraints: VideoConstraints,
    val videoCapturer: VideoCapturer
) : VideoTrack() {

    interface LocalVideoTrackEvents {

        fun setEnable(name: String, isEnable: Boolean)
    }

    override var executor: ExecutorService? = null

    override var internalVideoTrack: org.webrtc.VideoTrack? = null

    private var events: LocalVideoTrackEvents? = null

    private var surfaceTextureHelper: SurfaceTextureHelper? = null

    override fun isEnable(): Boolean {
        return internalVideoTrack?.enabled() ?: false
    }

    override fun enable(isEnable: Boolean) {
        internalVideoTrack?.let { track ->
            executor?.execute {
                track.setEnabled(isEnable)
            }
            events?.setEnable(name, isEnable)
        }
    }

    override fun addRenderer(renderer: VideoRenderer) {
        Log.d(TAG, "addRenderer")
        if (!videoRenderers.contains(renderer)) {
            videoRenderers.add(renderer)

            internalVideoTrack?.let {
                executor?.execute {
                    Log.d(TAG, "addSink")
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
        videoCapturer.dispose()
    }

    fun initInternalVideoTrack(
        internalVideoTrack: org.webrtc.VideoTrack, executorService: ExecutorService,
        events: LocalVideoTrackEvents, surfaceTextureHelper: SurfaceTextureHelper
    ) {
        Log.d(TAG, "initInternalVideoTrack")
        this.internalVideoTrack = internalVideoTrack
        this.executor = executorService
        this.events = events
        this.surfaceTextureHelper = surfaceTextureHelper

        if (videoRenderers.isNotEmpty()) {
            this.executor?.execute {
                for (renderer in videoRenderers) {
                    internalVideoTrack.addSink(renderer)
                }
            }
        }
    }

    companion object {
        private const val TAG = "[rtc]LocalVideoTrack"
    }
}