package com.src.webrtc.android

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import org.appspot.apprtc.PeerConnectionClient
import org.webrtc.*
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.concurrent.Executors

class Room private constructor(
    private val context: Context,
    private val connectParameters: ConnectParameters,
    private val roomListener: Listener.RoomListener
){

    val eglBase: EglBase = EglBase.create()

    private val executorService = Executors.newSingleThreadExecutor()

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var peerConnectionParameters: PeerConnectionClient.PeerConnectionParameters

    private val iceCandidatesRecord = mutableMapOf<String, MutableList<IceCandidate>>()

    var localPeer: LocalPeer? = null

    val remotePeers: Map<String, RemotePeer>
        get() = _remotePeers
    private val _remotePeers = mutableMapOf<String, RemotePeer>()

    fun connect() {
        Log.d(TAG, "connect")

        peerConnectionParameters = ConferenceParameters(context).getPeerConnectionParameters(connectParameters)

        localPeer = LocalPeer(connectParameters.localPeerId, context, eglBase, connectParameters, peerConnectionParameters, executorService)

        _remotePeers.forEach { (id, peer) ->
            Log.d(TAG, "remotePeer $id $peer")
        }

        roomListener.onConnected(this)
    }

    fun onPeerJoin(peerId: String) {
        Log.d(TAG, "onPeerJoin $peerId")

        if (_remotePeers.containsKey(peerId)) {
            Log.e(TAG, "Already has this remote peer $peerId")
            return
        }

        val remotePeer = RemotePeer(peerId, context, true, localPeer!!, eglBase, peerConnectionParameters, executorService, remotePeerEvents)
        _remotePeers[peerId] = remotePeer

        remotePeer.sdpHandshake()
    }

    fun onPeerLeave(peerId: String) {
        Log.d(TAG, "onPeerLeave $peerId")
        disconnectPeer(peerId)
    }

    fun disconnect() {
        Log.d(TAG, "disconnect")
        if (_remotePeers.isNotEmpty()) {
            _remotePeers.forEach { (_, peer) ->
                peer.release()
            }
            _remotePeers.clear()
        }

        localPeer!!.release()
        localPeer = null

        roomListener.onDisconnected(this)
    }

    fun onSdpReceived(peerId: String, type: String, sdp: String) {
        Log.d(TAG, "onSdpReceived $peerId $type $sdp")

        when (type) {
            SIG_TYPE_OFFER -> {
                if (_remotePeers.containsKey(peerId)) {
                    Log.e(TAG, "Already has this remote peer.")
                    return
                }

                val remotePeer = RemotePeer(peerId, context, false, localPeer!!, eglBase, peerConnectionParameters, executorService, remotePeerEvents)
                _remotePeers[peerId] = remotePeer

                val sessionDescription = SessionDescription(SessionDescription.Type.fromCanonicalForm(SIG_TYPE_OFFER), sdp)
                remotePeer.sdpHandshake()
                remotePeer.onRemoteDescription(sessionDescription)

                if (iceCandidatesRecord.isNotEmpty() && iceCandidatesRecord.containsKey(peerId)) {
                    iceCandidatesRecord[peerId]!!.forEach {
                        remotePeer.addRemoteIceCandidate(it)
                    }
                    iceCandidatesRecord.remove(peerId)
                }
            }
            SIG_TYPE_ANSWER -> {
                val remotePeer = _remotePeers[peerId]
                if (remotePeer == null) {
                    Log.e(TAG, "Can't find the remote peer.")
                    return
                }

                val sessionDescription = SessionDescription(SessionDescription.Type.fromCanonicalForm(SIG_TYPE_ANSWER), sdp)
                remotePeer.onRemoteDescription(sessionDescription)
            }
        }
    }

    fun onCandidateReceived(peerId: String, candidate: IceCandidate) {
        Log.d(TAG, "onCandidateReceived $peerId")

        val remotePeer = _remotePeers[peerId]
        if (remotePeer == null) {
            if (iceCandidatesRecord.containsKey(peerId)) {
                iceCandidatesRecord[peerId]!!.add(candidate)
            } else {
                val candidates = mutableListOf<IceCandidate>()
                candidates.add(candidate)
                iceCandidatesRecord[peerId] = candidates
            }
        } else {
            remotePeer.addRemoteIceCandidate(candidate)
        }
    }

    fun onCandidatesRemoveReceived(peerId: String, candidates: Array<out IceCandidate>) {
        Log.d(TAG, "onCandidatesRemoveReceived $peerId")
        val remotePeer = _remotePeers[peerId]
        if (remotePeer == null) {
            Log.e(TAG, "Can't find the remote peer $peerId")
            return
        }

        remotePeer.removeRemoteIceCandidates(candidates)
    }

    fun release() {
        Log.d(TAG, "release")
        executorService.shutdown()
        eglBase.release()
    }

    private val remotePeerEvents = object: RemotePeerEvents {
        override fun onLocalDescription(to: String, type: String, sdp: String) {
            handler.post {
                roomListener.onLocalDescription(to, type, sdp)
            }
        }

        override fun onIceCandidate(to: String, iceCandidate: IceCandidate) {
            handler.post {
                roomListener.onIceCandidate(to, iceCandidate)
            }
        }

        override fun onIceCandidatesRemoved(to: String, iceCandidates: Array<out IceCandidate>) {
            handler.post {
                roomListener.onIceCandidatesRemove(to, iceCandidates)
            }
        }

        override fun onIceConnected(id: String) {
            Log.d(TAG, "onIceConnected $id")
        }

        override fun onIceDisconnected(id: String) {
            Log.d(TAG, "onIceDisconnected $id")
        }

        override fun onConnected(id: String) {
            handler.post {
                Log.d(TAG, "onConnected $id")
                val remotePeer = _remotePeers[id]
                if (remotePeer == null) {
                    Log.e(TAG, "Remote peer $id not found")
                    return@post
                }

                roomListener.onPeerConnected(this@Room, remotePeer)
//                remotePeer.getAudioTracks().forEach { (_, track) ->
//                    remotePeerListener.onAudioTrackReady(remotePeer, track)
//                }
//                remotePeer.getVideoTracks().forEach { (_, track) ->
//                    remotePeerListener.onVideoTrackReady(remotePeer, track)
//                }
            }
        }

        override fun onDisconnected(id: String) {
            Log.d(TAG, "onDisconnected $id")
        }

//        override fun onDataChannel(id: String, label: String) {
//            handler.post {
//                Log.d(TAG, "onDataChannel $id $label")
//                val remotePeer = _remotePeers[id]
//                if (remotePeer == null) {
//                    Log.e(TAG, "Remote peer $id not found")
//                    return@post
//                }
//
//                remotePeer.getDataTracks()[label]?.let {
//                    remotePeerListener.onDataTrackReady(remotePeer, it)
//                }
//            }
//        }

//        override fun onMessage(id: String, label: String, buffer: DataChannel.Buffer) {
//            val copiedBuffer = DataChannel.Buffer(cloneByteBuffer(buffer.data), buffer.binary)
//            handler.post {
//                Log.d(TAG, "onMessage, id: $id, label: $label")
//
//                val remotePeer = _remotePeers[id]
//                if (remotePeer == null) {
//                    Log.e(TAG, "Remote peer $id not found")
//                    return@post
//                }
//
//                remotePeer.getDataTracks()[label]?.let {
//                    if (!copiedBuffer.binary) {
//                        val data = copiedBuffer.data
//                        val bytes = ByteArray(data.capacity())
//                        data.get(bytes)
//                        val msg = String(bytes, Charset.forName("UTF-8"))
//                        Log.d(TAG, "onMessage: $msg")
//                        remoteDataListener.onMessage(remotePeer, it, msg)
//                    } else {
//                        remoteDataListener.onMessage(remotePeer, it, copiedBuffer.data)
//                    }
//                }
//            }
//        }

        override fun onPeerConnectionClose(id: String) {
            Log.d(TAG, "onPeerConnectionClose $id")
        }

        override fun onPeerConnectionStatsReady(id: String, reports: Array<StatsReport>) {
            TODO("Not yet implemented")
        }

        override fun onPeerConnectionStatsReady(id: String, report: RTCStatsReport) {
            TODO("Not yet implemented")
        }

        override fun onPeerConnectionError(id: String, description: String) {
            Log.d(TAG, "onPeerConnectionError $id $description")

            disconnectPeer(id)
        }
    }

    private fun disconnectPeer(id: String) {
        handler.post {
            Log.d(TAG, "disconnectPeer $id")

            val remotePeer = _remotePeers.remove(id)
            if (remotePeer == null) {
                Log.e(TAG, "Can't find the remote peer $id")
                return@post
            }

            roomListener.onPeerDisconnected(this, remotePeer)
            remotePeer.release()
        }
    }

    private fun cloneByteBuffer(original: ByteBuffer): ByteBuffer {
        val clone = ByteBuffer.allocate(original.capacity())
        original.rewind()
        clone.put(original)
        original.rewind()
        clone.flip()
        return clone
    }

    companion object {

        private const val TAG = "[rtc]Room"

        fun connect(context: Context,
                    connectParameters: ConnectParameters,
                    roomListener: Listener.RoomListener
        ): Room {
            val room = Room(context, connectParameters, roomListener)
            room.connect()
            return room
        }
    }
}