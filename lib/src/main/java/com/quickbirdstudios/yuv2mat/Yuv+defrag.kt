package com.quickbirdstudios.yuv2mat

import android.util.Log
import java.nio.ByteBuffer

/*
################################################################################################
PUBLIC API
################################################################################################
*/

/**
 * @return [Yuv] with defragmented U and V planes.
 * @see defragPlane
 */
fun Yuv.defrag(): Yuv {
    val start = System.currentTimeMillis()

    require(y.pixelStride == 1)
    require(y.rowStride == width)

    val defragmented = copy(
        u = defragPlane(this.rows / 2, this.cols / 2, u),
        v = defragPlane(this.rows / 2, this.cols / 2, v))

    val time = System.currentTimeMillis() - start
    Log.w(YuvImage::class.java.simpleName, "de-fragmentation took ${time}ms")

    return defragmented
}


/**
 * @return [Yuv.Plane] with
 * pixel stride = 1
 * and
 * rowStride = cols
 */
private fun defragPlane(rows: Int, cols: Int, plane: Yuv.Plane): Yuv.Plane {
    /* Return the plane if it is not even fragmented */
    if (plane.pixelStride == 1 && plane.rowStride == cols) return plane

    /* Only remove padding */
    if (plane.pixelStride == 1) return defragPlanePadding(rows, cols, plane)

    /* Remove padding and pixel stride */
    return defragPlaneFull(rows, cols, plane)
}

private fun defragPlaneFull(rows: Int, cols: Int, plane: Yuv.Plane): Yuv.Plane {
    /*
    No move all data into the new defragmented ByteArray
     */
    val defragmented = ByteArray(rows * cols)
    val data = plane.buffer.toByteArray()

    for (row in 0 until rows) {
        for (col in 0 until cols) {
            val index = row * plane.rowStride + col * plane.pixelStride
            defragmented[row * cols + col] = data[index]
        }
    }

    return Yuv.Plane(
        buffer = ByteBuffer.wrap(defragmented),
        pixelStride = 1,
        rowStride = cols)
}

private fun defragPlanePadding(rows: Int, cols: Int, plane: Yuv.Plane): Yuv.Plane {
    require(plane.pixelStride == 1)
    val planeMatrix = BufferMatrix(plane.buffer, width = plane.rowStride, height = rows)
    val defragmented = byteBuffer(plane.buffer.isDirect, rows * cols)
    val colRange = 0 until cols

    for (row in 0 until rows) {
        val rowData = planeMatrix[row, colRange]
        defragmented.put(rowData)
    }

    return Yuv.Plane(
        buffer = defragmented,
        pixelStride = 1,
        rowStride = cols)
}


//endregion
