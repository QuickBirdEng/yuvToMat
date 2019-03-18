package com.quickbirdstudios.yuv2mat


/*
################################################################################################
INTERNAL API
################################################################################################
*/

internal infix fun BufferMatrix.with(clip: YuvImage.Clip): BufferMatrix {
    val pixels = clip.width.value * clip.height.value
    val columnRange = clip.left until clip.right
    val clipped = byteBuffer(this.data.isDirect, pixels)
    for (row in clip.top until clip.bottom) {
        val rowBuffer = this[row, columnRange]
        clipped.put(rowBuffer)
    }

    return BufferMatrix(
        data = clipped,
        width = clip.width.value,
        height = clip.height.value)
}



