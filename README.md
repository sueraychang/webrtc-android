# WebRTC Multi-Party Android Library

_For the WebRTC, go [here](https://webrtc.org/)._ 

## Overview
This is a WebRTC based Android Library to achieve multi-party video chat. 
The sample application demonstrates how to use the library to perform real-time communication with others.

![preview](https://github.com/sueraychang/webrtc-android/blob/master/screenshot.png)

## Setup Requirements

Before start, there are some resources needed to run the application.

### Signaling channel
Signaling is the communication process. In order for a WebRTC application to connect, the clients need to exchange information using the signaling channel. In the sample, I use Cloud Firestore as the signaling channel to deliver SDP and other messages with each other. _Get started with [Cloud Firestore](https://firebase.google.com/docs/firestore/quickstart)._

### ICE server
To deal with network address translation (NAT) and firewalls, an ICE server is needed. In the sample, I use the STUN servers WebRTC Android example uses.
```kotlin
private val ICE_URLS = listOf(
    "stun:stun1.l.google.com:19302",
    "stun:stun2.l.google.com:19302"
)
```
Because there is no TURN, please use the wifi network instead of cellular to run the sample.

## Start Video Chat
1. Connect to the room.
```kotlin
room = Room.connect(
    context,
    connectParameters,
    roomListener
)
```
2. Tell the room that a new peer is comming
```kotlin
fun onPeerJoin(peerId: String) {
    // When there is a peer join to the room, 
    room.onPeerJoin(peerId)
    // After calling this an offer is created (received from RoomListener.onLocalDescription).
}
```

3. Do the handshake to exchange SDP & ICE candidates
```kotlin
val roomListener = object : Listener.RoomListener {
    override fun onLocalDescription(to: String, type: SDPType, sdp: String) {
        // After onPeerJoin(), the offer is generated, we need to send it to others using signaling channel.
    }

    override fun onIceCandidate(to: String, iceCandidate: IceCandidate) {
        // We also need to send the ICE candidates to others.
    }
    ...
}
```

4. Listen to the new state of Room & Peer
```kotlin
val roomListener = object : Listener.RoomListener {
    ...
    override fun onConnected(room: Room) {
        // We can get the local peer instance from the room.
    }

    override fun onPeerConnected(room: Room, remotePeer: RemotePeer) {
        // The remote peer is connected, we can start the video chat now.
    }

    override fun onPeerDisconnected(room: Room, remotePeer: RemotePeer) {
        // The remote peer is disconnected, we need to release the related resources.
    }
    ...
}
```
## Multi-Party

Much the same as 1-to-1, you only need to decide who is the offerer and who is the answerer.  
In the sample, when there is a new peer join to the room, all the peers already in the room will be the offerer, and the new peer should be the answerer.  

## Examples

Check out the sample app in sample/ to see it in action.
