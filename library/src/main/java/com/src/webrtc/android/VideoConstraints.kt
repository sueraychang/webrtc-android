package com.src.webrtc.android

class VideoConstraints private constructor(builder: Builder) {

    val resolution = builder.resolution
    val fps = builder.fps

    class Builder {

        var resolution: Resolution = Resolution.FHD
            private set
        var fps = 30
            private set

        fun fps(fps: Int): Builder {
            this.fps = fps
            return this
        }

        fun resolution(resolution: Resolution): Builder {
            this.resolution = resolution
            return this
        }

        fun build(): VideoConstraints {
            check(resolution.width >= 0) { "Width MUST be more than 0" }
            check(resolution.height >= 0) { "Height MUST be more than 0" }
            check(fps >= 0) { "FPS MUST be more than 0" }
            return VideoConstraints(this)
        }
    }
}