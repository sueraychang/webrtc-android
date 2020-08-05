package com.src.webrtc.android

class DataTrackOptions private constructor(builder: Builder) {
    val ordered = builder.ordered
    val maxPacketLifeTime = builder.maxPacketLifeTime
    val maxRetransmits = builder.maxRetransmits

    class Builder {
        var ordered = true
            private set
        var maxPacketLifeTime = -1
            private set
        var maxRetransmits = -1
            private set

        /** Ordered transmission of messages. Default is `true`.  */
        fun ordered(ordered: Boolean): Builder {
            this.ordered = ordered
            return this
        }

        /** Maximum retransmit time in milliseconds.  */
        fun maxPacketLifeTime(maxPacketLifeTime: Int): Builder {
            this.maxPacketLifeTime = maxPacketLifeTime
            return this
        }

        /** Maximum number of retransmitted messages.  */
        fun maxRetransmits(maxRetransmits: Int): Builder {
            this.maxRetransmits = maxRetransmits
            return this
        }

        /**
         * Builds the data track options.
         *
         *
         * Max packet life time and max retransmits are mutually exclusive. This means that only
         * one of these values can be set to a non default value at a time otherwise a [ ] occurs.
         */
        fun build(): DataTrackOptions {
            return DataTrackOptions(this)
        }
    }
}