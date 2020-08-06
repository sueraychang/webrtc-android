package com.src.webrtc.android

class AudioOptions private constructor(builder: Builder) {
    val echoCancellation = builder.echoCancellation
    val autoGainControl = builder.autoGainControl
    val highPassFilter = builder.highPassFilter
    val noiseSuppression = builder.noiseSuppression

    class Builder {
        var echoCancellation = true
        var autoGainControl = true
        var highPassFilter = true
        var noiseSuppression = true

        /** Attempts to filter away the output signal from later inbound pickup.  */
        fun echoCancellation(echoCancellation: Boolean): Builder {
            this.echoCancellation = echoCancellation
            return this
        }

        /** Adjust the sensitivity of the local mic dynamically.  */
        fun autoGainControl(autoGainControl: Boolean): Builder {
            this.autoGainControl = autoGainControl
            return this
        }

        /** Remove background noise of lower frequences.  */
        fun highPassFilter(highpassFilter: Boolean): Builder {
            highPassFilter = highpassFilter
            return this
        }

        /** Filter out background noise.  */
        fun noiseSuppression(noiseSuppression: Boolean): Builder {
            this.noiseSuppression = noiseSuppression
            return this
        }

        fun build(): AudioOptions {
            return AudioOptions(this)
        }
    }
}