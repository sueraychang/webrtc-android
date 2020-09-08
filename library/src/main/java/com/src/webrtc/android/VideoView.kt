package com.src.webrtc.android

import android.content.Context
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer

class VideoView : SurfaceViewRenderer {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: android.util.AttributeSet?) : super(context, attrs)

    fun init(rendererEvents: RendererCommon.RendererEvents?) {
        super.init(EglBaseProvider.getEglBase(this).eglBaseContext, rendererEvents)
    }

    override fun release() {
        super.release()
        EglBaseProvider.release(this)
    }
}