package com.src.webrtc.android

import android.content.Context
import org.appspot.apprtc.PeerConnectionClient
import org.webrtc.EglBase
import java.util.concurrent.ExecutorService

abstract class Peer(
    val id: String,
    protected val context: Context,
    peerConnectionParameters: PeerConnectionParameters,
    executorService: ExecutorService
) : PeerConnectionClient(context, null, peerConnectionParameters, null, executorService) {

    abstract fun release()
}