package com.quickbirdstudios.yuv2mat

import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.ByteBuffer

class ByteBufferClipTest {

    @Test
    internal fun clipDirectBuffer_start2_end8() {
        val directBuffer = ByteBuffer.allocateDirect(12)
        directBuffer.put(byteArrayOf(
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11))

        val clippedBuffer = directBuffer.clip(2 until 8)

        assertEquals(6, clippedBuffer.remaining())

        assertEquals(2.b, clippedBuffer[0])
        assertEquals(3.b, clippedBuffer[1])
        assertEquals(4.b, clippedBuffer[2])
        assertEquals(5.b, clippedBuffer[3])
        assertEquals(6.b, clippedBuffer[4])
        assertEquals(7.b, clippedBuffer[5])
    }

    @Test
    internal fun clipDirectBuffer_start0_end1() {
        val directBuffer = ByteBuffer.allocateDirect(12)
        directBuffer.put(byteArrayOf(
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11))

        val clippedBuffer = directBuffer.clip(0 until 1)

        assertEquals(1, clippedBuffer.remaining())
        assertEquals(0.b, clippedBuffer[0])
    }
}

private val Int.b get () = this.toByte()