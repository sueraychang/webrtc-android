package com.src.webrtc.android

import org.json.JSONObject
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.RTCStatsReport
import org.webrtc.StatsReport

interface RemotePeerEvents {
    fun onLocalDescription(to: String, type: String, sdp: String)
    fun onIceCandidate(to: String, iceCandidate: IceCandidate)
    fun onIceCandidatesRemoved(to: String, iceCandidates: Array<out IceCandidate>)
    fun onIceConnected(id: String)
    fun onIceDisconnected(id: String)
    fun onConnected(id: String)
    fun onDisconnected(id: String)
    fun onDataChannel(id: String, label: String)
    fun onMessage(
        id: String,
        label: String,
        buffer: DataChannel.Buffer
    )

    fun onPeerConnectionClose(id: String)
    fun onPeerConnectionStatsReady(
        id: String,
        reports: Array<StatsReport>
    )

    fun onPeerConnectionStatsReady(id: String, report: RTCStatsReport)
    fun onPeerConnectionError(id: String, description: String)
}