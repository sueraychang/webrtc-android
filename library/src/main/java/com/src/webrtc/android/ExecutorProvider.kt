package com.src.webrtc.android

import android.util.Log
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object ExecutorProvider {

    private const val TAG = "[rtc]ExecutorProvider"

    private var executor: ExecutorService? = null

    fun get(): ExecutorService {
        Log.d(TAG, "get")
        if (executor == null) {
            executor = Executors.newSingleThreadExecutor()
        }
        return executor!!
    }

    fun release() {
        Log.d(TAG, "release")
        executor!!.shutdown()
        executor = null
    }
}