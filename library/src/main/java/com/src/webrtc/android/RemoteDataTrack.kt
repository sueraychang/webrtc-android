package com.src.webrtc.android

import android.os.Handler
import android.os.Looper
import android.util.Log
import org.webrtc.DataChannel
import java.nio.ByteBuffer
import java.nio.charset.Charset

class RemoteDataTrack(
    override val id: String,
    private val dataChannel: DataChannel
) : DataTrack() {

    private var messageListener: MessageListener? = null

    private val handler = Handler(Looper.getMainLooper())

    init {
        dataChannel.registerObserver(object: DataChannel.Observer {
            override fun onBufferedAmountChange(p0: Long) {

            }

            override fun onStateChange() {

            }

            override fun onMessage(buffer: DataChannel.Buffer) {
                Log.d(TAG, "onMessage ${dataChannel.label()} ${dataChannel.state()}")
                messageListener?.let {
                    val copiedBuffer = DataChannel.Buffer(cloneByteBuffer(buffer.data), buffer.binary)
                    handler.post {
                        Log.d(TAG, "onMessage, label: ${dataChannel.label()}")
                        if (!copiedBuffer.binary) {
                            val data = copiedBuffer.data
                            val bytes = ByteArray(data.capacity())
                            data.get(bytes)
                            val msg = String(bytes, Charset.forName("UTF-8"))
                            Log.d(TAG, "onMessage: $msg")
                            it.onMessage(this@RemoteDataTrack, msg)
                        } else {
                            it.onMessage(this@RemoteDataTrack, copiedBuffer.data)
                        }
                    }
                }
            }
        })
    }

    override fun release() {
        dataChannel.dispose()
    }

    fun registerMessageListener(messageListener: MessageListener?) {
        this.messageListener = messageListener
    }

    companion object {
        private const val TAG = "[rtc]RemoteDataTrack"

        private fun cloneByteBuffer(original: ByteBuffer): ByteBuffer {
            val clone = ByteBuffer.allocate(original.capacity())
            original.rewind()
            clone.put(original)
            original.rewind()
            clone.flip()
            return clone
        }
    }
}