package com.src.webrtc.android

import android.content.Context
import android.util.Log
import org.webrtc.VideoCapturer
import java.util.concurrent.ExecutorService

class LocalVideoTrack(
    context: Context,
    override val id: String,
    videoConstraints: VideoConstraints,
    private val videoCapturer: VideoCapturer
) : VideoTrack() {

    override var executor: ExecutorService? = ExecutorProvider.get()

    override var internalVideoTrack: org.webrtc.VideoTrack? = null

//    private var surfaceTextureHelper: SurfaceTextureHelper? = null

    override fun isEnable(): Boolean {
        return internalVideoTrack?.enabled() ?: false
    }

    override fun enable(isEnable: Boolean) {
        executor?.execute {
            internalVideoTrack?.let { track ->
                track.setEnabled(isEnable)
            }
        }
    }

    override fun addRenderer(renderer: VideoRenderer) {
        Log.d(TAG, "addRenderer")
        if (!videoRenderers.contains(renderer)) {
            videoRenderers.add(renderer)

            executor?.execute {
                internalVideoTrack?.let {
                    Log.d(TAG, "addSink")
                    it.addSink(renderer)
                }
            }
        }
    }

    override fun removeRenderer(renderer: VideoRenderer) {
        if (videoRenderers.contains(renderer)) {
            videoRenderers.remove(renderer)

            executor?.execute {
                internalVideoTrack?.let {
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

    init {
        MediaFactory.get(context).createVideoTrack(context, id, videoConstraints, videoCapturer) {
            internalVideoTrack = it
        }
    }

//    fun initInternalVideoTrack(
//        internalVideoTrack: org.webrtc.VideoTrack, executorService: ExecutorService, surfaceTextureHelper: SurfaceTextureHelper
//    ) {
//        Log.d(TAG, "initInternalVideoTrack")
//        this.internalVideoTrack = internalVideoTrack
//        this.executor = executorService
//        this.surfaceTextureHelper = surfaceTextureHelper
//
//        if (videoRenderers.isNotEmpty()) {
//            this.executor?.execute {
//                for (renderer in videoRenderers) {
//                    internalVideoTrack.addSink(renderer)
//                }
//            }
//        }
//    }

    companion object {
        private const val TAG = "[rtc]LocalVideoTrack"
    }
}