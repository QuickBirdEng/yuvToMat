package com.quickbirdstudios.yuv2mat.internal

import android.media.Image
import com.quickbirdstudios.yuv2mat.Clip
import java.nio.ByteBuffer

/*
################################################################################################
INTERNAL API
################################################################################################
*/

/**
 * Abstraction for yuv_420_888 images provided by the camera preview
 */

internal interface Yuv420888Image {
    val width: Int
    val height: Int
    val yPlane: ByteBuffer
    val uPlane: ByteBuffer
    val vPlane: ByteBuffer
    val uPixelStride: Int
    val vPixelStride: Int

    /**
     * Represents the whole as one big byte-buffer.
     * It consists of all Y components, followed by U components followed by all V components
     */
    val data: ByteBuffer

    /**
     * Clips the data according to the specified area.
     * The clip is only accurate up to two pixels, because clipping is only
     * supported in the 2x2 yuv grid.
     * (see: https://en.wikipedia.org/wiki/YUV#Y%E2%80%B2UV420p_(and_Y%E2%80%B2V12_or_YV12)_to_RGB888_conversion)
     */
    fun clip(clip: Clip): Yuv420888Image

    companion object
}

/*
 ###################################################################################################
 FACTORY METHODS
 ###################################################################################################
 */

internal operator fun Yuv420888Image.Companion.invoke(
    width: Int,
    height: Int,
    yPlane: ByteBuffer,
    uPlane: ByteBuffer,
    vPlane: ByteBuffer,
    uPixelStride: Int,
    vPixelStride: Int): Yuv420888Image = Yuv420888ImageImpl(
    width = width,
    height = height,
    yPlane = yPlane,
    uPlane = uPlane,
    vPlane = vPlane,
    uPixelStride = uPixelStride,
    vPixelStride = vPixelStride)

internal operator fun Yuv420888Image.Companion.invoke(image: Image)
    : Yuv420888Image = Yuv420888ImageImpl(
    width = image.width,
    height = image.height,
    yPlane = image.planes[0].buffer,
    uPlane = image.planes[1].buffer,
    vPlane = image.planes[2].buffer,
    uPixelStride = image.planes[1].pixelStride,
    vPixelStride = image.planes[2].pixelStride)


/*
####################################################################################################
Simple implementation
####################################################################################################
*/

private class Yuv420888ImageImpl internal constructor(
    override val width: Int,
    override val height: Int,
    override val yPlane: ByteBuffer,
    override val uPlane: ByteBuffer,
    override val vPlane: ByteBuffer,
    override val uPixelStride: Int,
    override val vPixelStride: Int) : Yuv420888Image {

    override val data by lazy {
        yPlane.position(0)
        uPlane.position(0)
        vPlane.position(0)

        /*
        We need to use allocateDirect instead of allocate to ensure, that we
        avoid copying data into the jvm as much as possible!
         */
        val data = ByteBuffer.allocateDirect(
            yPlane.remaining() +
                uPlane.remaining() +
                vPlane.remaining())

        data.put(yPlane)
        data.put(uPlane)
        data.put(vPlane)
        data.position(0)
        data
    }


    override fun clip(clip: Clip): Yuv420888Image {
        @Suppress("NAME_SHADOWING")
        val clip = clip.adjustToYuvGrid()
        val clippedYPlane = clipYPlane(clip)
        val clippedUPlane = clipUPlane(clip)
        val clippedVPlane = clipVPlane(clip)

        return Yuv420888ImageImpl(
            width = clip.right - clip.left,
            height = clip.bottom - clip.top,
            yPlane = clippedYPlane,
            uPlane = clippedUPlane,
            vPlane = clippedVPlane,
            uPixelStride = uPixelStride,
            vPixelStride = vPixelStride)
    }

    /*
    ################################################################################################
    CLIP: private helper functions
    ################################################################################################
     */

    private fun clipYPlane(clip: Clip): ByteBuffer {
        val yBufferMatrix = BufferMatrix(yPlane, this.width, this.height)
        return clipMatrix(yBufferMatrix, clip)
    }

    private fun clipUPlane(clip: Clip): ByteBuffer {
        val uClip = uPlaneClip(clip)
        val uBufferMatrix = uPlaneBufferMatrix()
        return clipMatrix(uBufferMatrix, uClip)
    }

    private fun clipVPlane(clip: Clip): ByteBuffer {
        val vClip = vPlaneClip(clip)
        val vBufferMatrix = vPlaneBufferMatrix()
        return clipMatrix(vBufferMatrix, vClip)
    }

    private fun uPlaneClip(clip: Clip): Clip {
        return uvPlaneClip(clip, uPixelStride)
    }

    private fun vPlaneClip(clip: Clip): Clip {
        return uvPlaneClip(clip, vPixelStride)
    }

    /**
     * Transforms the clip for a u or v plane with given stride.
     */
    private fun uvPlaneClip(clip: Clip, stride: Int): Clip {
        /*
        We generally just have half as many rows and columns in
        u or v planes (.div(2)).

        The amount of values in one row need to be multiplied by stride
         */
        return Clip(
            left = clip.left.div(2).times(stride),
            top = clip.top.div(2),
            right = clip.right.div(2).times(stride),
            bottom = clip.bottom.div(2))
    }

    private fun uPlaneBufferMatrix(): BufferMatrix {
        return uvPlaneBufferMatrix(uPlane, uPixelStride)
    }

    private fun vPlaneBufferMatrix(): BufferMatrix {
        return uvPlaneBufferMatrix(vPlane, vPixelStride)
    }

    /**
     * @return A [BufferMatrix] for u or v plane with given stride
     */
    private fun uvPlaneBufferMatrix(plane: ByteBuffer, stride: Int): BufferMatrix {
        return BufferMatrix(
            data = plane,
            /*
            Generally we have only half the pixels in one row, but
            the stride needs to be multiplied, because of the empty spots in the data
             */
            width = this.width.div(2).times(stride),
            height = this.height.div(2))
    }

    /**
     * Will clip the buffer matrix
     * @return A [ByteBuffer] representing the clipped matrix
     */
    private fun clipMatrix(buffer: BufferMatrix, clip: Clip): ByteBuffer {
        val clipWidth = clip.right - clip.left
        val clipHeight = clip.bottom - clip.top
        val pixels = clipWidth * clipHeight
        val columnRange = clip.left until clip.right

        /*
        We are using allocateDirect in favor of allocate to avoid copying into the
        jvm as much as possible
         */
        val clipped = ByteBuffer.allocateDirect(pixels)
        for (row in clip.top until clip.bottom) {
            val rowData = buffer[row, columnRange]
            clipped.put(rowData)
        }

        return clipped
    }


    /**
     * Clipping is only supported inn the 2x2 grid of the yuv format.
     * We cannot clip at odd positions.
     *
     * @return A new clip with all elements % 2 == 0
     *
     */
    private fun Clip.adjustToYuvGrid(): Clip {
        fun Int.isInGrid() = this % 2 == 0
        val left = if (this.left.isInGrid()) this.left else this.left - 1
        val top = if (this.top.isInGrid()) this.top else this.top - 1
        val right = if (this.right.isInGrid()) this.right else right + 1
        val bottom = if (this.bottom.isInGrid()) this.bottom else this.bottom + 1

        return Clip(
            left = left,
            top = top,
            right = right,
            bottom = bottom)
    }

    private operator fun Clip.times(other: Int): Clip {
        return Clip(
            left = this.left * other,
            top = this.top * other,
            right = this.right * other,
            bottom = this.bottom * other)
    }

    private operator fun Clip.div(other: Int): Clip {
        return Clip(
            left = this.left / other,
            top = this.top / other,
            right = this.right / other,
            bottom = this.bottom / other)
    }


    /**
     * Helper class to wrap a given [ByteBuffer] into matrix representation.
     * This class is useful to retrieve data from the buffer under given row and col
     */
    private class BufferMatrix(val data: ByteBuffer,
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
}