package com.quickbirdstudios.yuv2mat

import android.graphics.ImageFormat
import android.media.Image
import com.roche.greendot.pipeline.yuv.YuvPlanes
import java.nio.ByteBuffer

/*
################################################################################################
PUBLIC API
################################################################################################
*/

data class Yuv(
    val resource: AutoCloseable? = null,
    val width: Int,
    val height: Int,
    val y: Plane,
    val u: Plane,
    val v: Plane) : YuvPlanes, AutoCloseable {

    constructor(image: Image) : this(
        resource = image,
        width = image.width,
        height = image.height,
        y = Plane(image.planes[0]),
        u = Plane(image.planes[1]),
        v = Plane(image.planes[2])) {
        require(image.format == ImageFormat.YUV_420_888)
        require(image.width % 2 == 0)
        require(image.height % 2 == 0)
    }

    data class Plane(
        val buffer: ByteBuffer,
        val pixelStride: Int,
        val rowStride: Int) {
        constructor(plane: Image.Plane) : this(
            buffer = plane.buffer,
            pixelStride = plane.pixelStride,
            rowStride = plane.rowStride)
    }


    override fun close() {
        resource?.close()
    }

    val rows = this.height
    val cols = this.width
}