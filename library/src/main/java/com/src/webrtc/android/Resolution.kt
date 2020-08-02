package com.src.webrtc.android

class Resolution(
    val width: Int,
    val height: Int
) {

    companion object {
        const val QVGA_WIDTH = 320
        const val QVGA_HEIGHT = 240
        val QVGA = Resolution(QVGA_WIDTH, QVGA_HEIGHT)

        const val VGA_WIDTH = 640
        const val VGA_HEIGHT = 480
        val VGA = Resolution(VGA_WIDTH, VGA_HEIGHT)

        const val HD_WIDTH = 1280
        const val HD_HEIGHT = 720
        val HD = Resolution(HD_WIDTH, HD_HEIGHT)

        const val FHD_WIDTH = 1920
        const val FHD_HEIGHT = 1080
        val FHD = Resolution(FHD_WIDTH, FHD_HEIGHT)
    }
}