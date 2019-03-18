package com.quickbirdstudios.yuv2mat

import org.junit.Assert.assertArrayEquals
import org.junit.Test
import java.nio.ByteBuffer

class BufferMatrixWithClipTest {
    @Test
    fun arbitraryClip() {
        val data = byteArrayOf(
            0, 0, 0, 0, 0, 0,
            1, 1, 1, 1, 1, 1,
            2, 2, 3, 3, 4, 4,
            5, 5, 6, 6, 7, 7,
            8, 8, 8, 8, 8, 8)

        val matrix = BufferMatrix(data = ByteBuffer.wrap(data), width = 6, height = 5)
        val clip = YuvImage.Clip(left = 2, top = 2, right = 4, bottom = 4)
        val clipped = matrix.with(clip).data

        assertArrayEquals(byteArrayOf(3, 3, 6, 6), clipped.toByteArray())
    }
}