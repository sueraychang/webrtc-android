package com.src.webrtc.android

import org.webrtc.IceCandidate
import java.nio.ByteBuffer

interface Listener {

    interface RoomListener {

        fun onLocalDescription(to: String, type: String, sdp: String)

        fun onIceCandidate(to: String, iceCandidate: IceCandidate)

        fun onIceCandidatesRemove(to: String, iceCandidates: Array<out IceCandidate>)

        fun onConnected(room: Room)

        fun onConnectFailed(room: Room)

        fun onDisconnected(room: Room)

        fun onPeerConnected(room: Room, remotePeer: RemotePeer)

        fun onPeerDisconnected(room: Room, remotePeer: RemotePeer)
    }

    interface RemotePeerListener {

        fun onAudioTrackReady(remotePeer: RemotePeer, remoteAudioTrack: RemoteAudioTrack)

        fun onVideoTrackReady(remotePeer: RemotePeer, remoteVideoTrack: RemoteVideoTrack)

        fun onDataTrackReady(remotePeer: RemotePeer, remoteDataTrack: RemoteDataTrack)
    }

    interface RemoteDataListener {

        fun onMessage(
            remotePeer: RemotePeer,
            remoteDataTrack: RemoteDataTrack,
            byteBuffer: ByteBuffer
        )

        fun onMessage(remotePeer: RemotePeer, remoteDataTrack: RemoteDataTrack, message: String)
    }

    interface CameraSwitchListener {

        fun onCameraSwitchDone(isFrontCamera: Boolean)

        fun onCameraSwitchError(error: String)
    }
}