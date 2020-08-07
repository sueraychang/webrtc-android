package com.src.webrtc.android

import android.util.Log
import org.webrtc.DataChannel
import java.nio.ByteBuffer
import java.nio.charset.Charset

class LocalDataTrack(
    override val id: String,
    val options: DataTrackOptions
) : DataTrack() {

    private val internalDataChannels = mutableMapOf<String, DataChannel>()

    fun send(messageBuffer: ByteBuffer): Boolean {
        if (internalDataChannels.isEmpty()) {
            return false
        }

        internalDataChannels.forEach { (id, channel) ->
            val result = channel.send(DataChannel.Buffer(messageBuffer, true))
            Log.d(TAG, "send ByteBuffer to $id, result: $result")
        }
        return true
    }

    fun send(message: String): Boolean {
        if (internalDataChannels.isEmpty()) {
            return false
        }

        internalDataChannels.forEach { (id, channel) ->
            val buff = Charset.forName("UTF-8").encode(message)
            val result = channel.send(DataChannel.Buffer(buff, false))
            Log.d(TAG, "send message to " + id + ", label:" + channel.label() + " result: " + result)
        }
        return true
    }

    override fun release() {
        internalDataChannels.forEach { (_, channel) ->
            channel.dispose()
        }
        internalDataChannels.clear()
    }

    fun initInternalDataChannel(id: String, internalDataChannel: DataChannel) {
        internalDataChannels[id] = internalDataChannel
    }

    companion object {
        private const val TAG = "[rtc]LocalDataTrack"
    }
}