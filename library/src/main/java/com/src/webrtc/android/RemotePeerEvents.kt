package com.src.webrtc.android

import org.webrtc.IceCandidate
import org.webrtc.RTCStatsReport
import org.webrtc.StatsReport

interface RemotePeerEvents {
    fun onLocalDescription(to: String, type: SDPType, sdp: String)
    fun onIceCandidate(to: String, iceCandidate: IceCandidate)
    fun onIceCandidatesRemoved(to: String, iceCandidates: Array<out IceCandidate>)
    fun onIceConnected(id: String)
    fun onIceDisconnected(id: String)
    fun onConnected(id: String)
    fun onDisconnected(id: String)
    fun onPeerConnectionClose(id: String)
    fun onPeerConnectionStatsReady(id: String, reports: Array<StatsReport>)
    fun onPeerConnectionStatsReady(id: String, report: RTCStatsReport)
    fun onPeerConnectionError(id: String, description: String)
}