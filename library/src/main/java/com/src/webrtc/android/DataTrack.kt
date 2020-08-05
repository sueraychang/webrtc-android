package com.src.webrtc.android

import org.webrtc.DataChannel

abstract class DataTrack: Track {

    interface Events {

        fun onMessage(label: String, buffer: DataChannel.Buffer)
    }

    abstract fun release()
}