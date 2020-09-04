package com.src.webrtc.android

import org.webrtc.EglBase

object EglBaseProvider {

    private val owners = mutableSetOf<Any>()

    private var eglBase: EglBase? = null

    fun getEglBase(owner: Any): EglBase {
        if (owners.isEmpty()) {
            eglBase = EglBase.create()
        }
        owners.add(owner)
        return eglBase!!
    }

    fun release(owner: Any) {
        owners.remove(owner)
        if (owners.isEmpty()) {
            eglBase!!.release()
            eglBase = null
        }
    }
}