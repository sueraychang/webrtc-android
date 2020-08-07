package com.src.webrtc.android

import java.nio.ByteBuffer

abstract class DataTrack : Track {

    interface MessageListener {

        fun onMessage(remoteDataTrack: RemoteDataTrack, byteBuffer: ByteBuffer)

        fun onMessage(remoteDataTrack: RemoteDataTrack, message: String)
    }

    abstract fun release()
}