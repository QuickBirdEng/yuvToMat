package com.quickbirdstudios.yuv2mat

import java.nio.ByteBuffer

/*
################################################################################################
INTERNAL API
################################################################################################
*/

/**
 * Helper class to wrap a given [ByteBuffer] into matrix representation.
 * This class is useful to retrieve data from the buffer under given row and col
 */
internal class BufferMatrix(
    val data: ByteBuffer,
    val width: Int,
    val height: Int) {

    val pixels = width * height

    operator fun get(row: Int, col: IntRange): ByteBuffer {
        val firstIndexOfRow = width * row
        val firstColumn = firstIndexOfRow + col.start
        val lastColumn = firstIndexOfRow + col.last
        return data.clip(firstColumn..lastColumn)
    }

    operator fun get(row: Int, col: Int): ByteBuffer {
        val firstIndexOfRow = width * row
        val index = firstIndexOfRow + col
        return data.clip(index..index)
    }

    init {
        if (pixels != data.limit()) {
            IllegalArgumentException("Expected width*height==data.limit(). " +
                "width*height=$pixels, data.limit()=${data.limit()}")
        }
    }
}