package com.src.webrtc.android.sample.data

data class Candidate(
    val sdpMid: String = "",
    val sdpMLineIndex: Int = 0,
    val candidate: String = ""
)

data class SdpCandidate(
    val from: String = "",
    val to: String = "",
    val candidate: Candidate = Candidate()
)

data class SdpCandidatesRemove(
    val from: String = "",
    val to: String = "",
    val candidates: List<Candidate> = emptyList()
)