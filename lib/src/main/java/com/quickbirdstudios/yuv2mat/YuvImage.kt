package com.quickbirdstudios.yuv2mat

import android.graphics.ImageFormat
import android.media.Image
import org.opencv.core.Mat

/*
################################################################################################
PUBLIC API
################################################################################################
*/

/**
 * Represents an image taken in the yuv 420_888 format ([ImageFormat.YUV_420_888]).
 * Implementations are intended to support high efficiency conversion into
 * the rgb color space.
 *
 *
 * ### Pre-Clipping
 * Using the [clip] function before converting into rgb color space is generally preferred than
 * vice versa:
 *
 * ```
 * YuvImage(image).clip(...).toRgb()
 * ```
 *
 * Is often much faster than
 *
 * ```
 * YuvImage(image).toRgb().submat(...)
 * ```
 */
interface YuvImage : AutoCloseable {

    infix fun with(clip: Clip): YuvImage

    fun clip(left: Int, top: Int, right: Int, bottom: Int): YuvImage =
        this with Clip(left = left, top = top, right = right, bottom = bottom)

    fun toRgb(): Mat

    data class Clip(val left: Int, val top: Int, val right: Int, val bottom: Int) {

        interface Dimension {
            val value: Int
            operator fun times(value: Int): Clip
            operator fun div(value: Int): Clip
        }


        val width = object : Dimension {

            override val value: Int = right - left

            override fun times(value: Int): Clip = copy(
                left = left * value,
                right = right * value)

            override fun div(value: Int): Clip = copy(
                left = left / value,
                right = right / value)
        }

        val height = object : Dimension {

            override val value: Int = bottom - top

            override fun times(value: Int): Clip = copy(
                top = top * value,
                bottom = bottom * value)


            override fun div(value: Int): Clip = copy(
                top = top / value,
                bottom = bottom / value)

        }


        init {
            require(left < right)
            require(top < bottom)
            require(left >= 0)
            require(top >= 0)
            require(right >= 0)
            require(bottom >= 0)
        }
    }

    companion object Factory
}


/**
 * Thrown by [YuvImage.Factory.invoke] for non supported yuv formats.
 * According to the Android documentation no phone should encounter this exception.
 */
class UnsupportedImageFormatException(yuv: Yuv) : Exception("" +
    "Yuv format is not supported: $yuv")


/**
 * Same as
 * ```
 * YuvImage(Yuv(image))
 * ```
 */
@Throws(UnsupportedImageFormatException::class)
operator fun YuvImage.Factory.invoke(image: Image): YuvImage {
    val yuv = Yuv(image)
    return YuvImage(yuv)
}

/**
 * @return The best performing implementation of [YuvImage] for the given data format
 * @throws UnsupportedImageFormatException: Not expected when captured by an android device
 */
@Throws(UnsupportedImageFormatException::class)
operator fun YuvImage.Factory.invoke(yuv: Yuv): YuvImage {
    if (yuv.y.pixelStride != 1 || yuv.y.rowStride != yuv.width) {
        throw UnsupportedImageFormatException(yuv)
    }

    return YuvN12Image.create(yuv)
        ?: YuvI420Image.create(yuv)
        ?: YuvI420Image.create(yuv.defrag())
        ?: throw UnsupportedImageFormatException(yuv)
}





