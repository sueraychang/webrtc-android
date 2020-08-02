package com.src.webrtc.android

import org.webrtc.VideoFrame
import org.webrtc.VideoSink

class VideoRenderer: VideoSink {

    private var target: VideoSink? = null

    override fun onFrame(frame: VideoFrame) {
        target?.onFrame(frame)
    }

    @Synchronized fun setTarget(target: VideoSink?) {
        this.target = target
    }
}