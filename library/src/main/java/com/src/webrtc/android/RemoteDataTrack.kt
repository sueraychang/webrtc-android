package com.src.webrtc.android

import android.util.Log
import org.webrtc.DataChannel

class RemoteDataTrack(
    override val name: String,
    private val dataChannel: DataChannel,
    private val events: Events
) : DataTrack() {

    init {
        dataChannel.registerObserver(object: DataChannel.Observer {
            override fun onBufferedAmountChange(p0: Long) {

            }

            override fun onStateChange() {

            }

            override fun onMessage(buffer: DataChannel.Buffer) {
                Log.d(TAG, "onMessage ${dataChannel.label()} ${dataChannel.state()}")
                events.onMessage(dataChannel.label(), buffer)
            }
        })
    }

    override fun release() {
        dataChannel.dispose()
    }

    companion object {
        private const val TAG = "[rtc]RemoteDataTrack"
    }
}