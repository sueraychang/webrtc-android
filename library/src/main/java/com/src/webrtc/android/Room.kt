package com.src.webrtc.android

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import org.appspot.apprtc.PeerConnectionClient
import org.webrtc.*
import java.util.concurrent.Executors

class Room private constructor(
    private val context: Context,
    private val connectParameters: ConnectParameters,
    private val roomListener: Listener.RoomListener,
    private val remotePeerListener: Listener.RemotePeerListener,
    private val remoteDataListener: Listener.RemoteDataListener
){

    val eglBase = EglBase.create()

    private val executorService = Executors.newSingleThreadExecutor()

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var peerConnectionParameters: PeerConnectionClient.PeerConnectionParameters

    private val iceCandidatesRecord = mutableMapOf<String, MutableList<IceCandidate>>()

    lateinit var localPeer: LocalPeer

    val remotePeers: Map<String, RemotePeer>
        get() = _remotePeers
    private val _remotePeers = mutableMapOf<String, RemotePeer>()

    fun connect() {
        Log.d(TAG, "connect")

        peerConnectionParameters = ConferenceParameters(context).getPeerConnectionParameters(connectParameters)

        localPeer = LocalPeer(connectParameters.localPeerId, context, eglBase, connectParameters, peerConnectionParameters, executorService)

        roomListener.onConnected(this)
    }

    fun onPeerJoin(peerId: String) {
        Log.d(TAG, "onPeerJoin $peerId")

        if (_remotePeers.containsKey(peerId)) {
            Log.e(TAG, "Already has this remote peer $peerId")
            return
        }

        val remotePeer = RemotePeer(peerId, context, true, localPeer, eglBase, peerConnectionParameters, executorService, remotePeerEvents)
        _remotePeers[peerId] = remotePeer

        remotePeer.sdpHandshake()
    }

    fun onPeerLeave(peerId: String) {
        Log.d(TAG, "onPeerLeave $peerId")

        val remotePeer = _remotePeers[peerId]
        if (remotePeer == null) {
            Log.e(TAG, "Can't find the remote peer $peerId")
            return
        }

        remotePeer.release()
        _remotePeers.remove(peerId)
        roomListener.onPeerDisconnected(this, remotePeer)
    }

    fun disconnect() {
        Log.d(TAG, "disconnect")
        if (_remotePeers.isNotEmpty()) {
            _remotePeers.forEach { (_, peer) ->
                peer.release()
            }
            _remotePeers.clear()
        }
        if (::localPeer.isInitialized) {
            localPeer.release()
        }

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

                val remotePeer = RemotePeer(peerId, context, false, localPeer, eglBase, peerConnectionParameters, executorService, remotePeerEvents)
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
            }
        }

        override fun onDisconnected(id: String) {
            Log.d(TAG, "onDisconnected $id")
        }

        override fun onDataChannel(id: String, label: String) {
            TODO("Not yet implemented")
        }

        override fun onMessage(id: String, label: String, buffer: DataChannel.Buffer) {
            TODO("Not yet implemented")
        }

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
        }
    }

    companion object {

        private const val TAG = "[rtc]Room"

        fun connect(context: Context,
                    connectParameters: ConnectParameters,
                    roomListener: Listener.RoomListener,
                    remotePeerListener: Listener.RemotePeerListener,
                    remoteDataListener: Listener.RemoteDataListener
        ): Room {
            val room = Room(context, connectParameters, roomListener, remotePeerListener, remoteDataListener)
            room.connect()
            return room
        }
    }
}