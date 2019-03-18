package com.quickbirdstudios.yuv2mat

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.opencv.android.OpenCVLoader
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.nio.ByteBuffer

/**
 * This test will use OpenCv to create synthetic yuv data.
 * The resulting [YuvImage] will then be compared to the source rgb
 */
class YuvN12ImageTest {

    private lateinit var rgbSource: Mat
    private lateinit var yuv: Yuv
    private lateinit var yuvImage: YuvImage

    @Before
    fun setup() {
        OpenCVLoader.initDebug()

        /* Arbitrary  rows and cols */
        val rows = 100
        val cols = 100
        val pixels = rows * cols

        /* Arbitrary rgb image */
        rgbSource = Mat(rows, cols, CvType.CV_8UC3).forEach { row, col ->
            val r = row.toByte()
            val g = col.toByte()
            val b = (r + g / 2).toByte()
            put(row, col, byteArrayOf(r, g, b))
        }

        /*
        Create yuv data. OpenCv only supports conversion in I420, which we
        then have to format as N12
         */
        val yuvMat = Mat(rows + rows / 2, cols, CvType.CV_8UC1).also { yuvMat ->
            Imgproc.cvtColor(rgbSource, yuvMat, Imgproc.COLOR_RGB2YUV_I420)
        }


        /* Get Y, U and V values separately from the yuv mat */
        val yuvData = ByteArray(pixels + pixels / 2).apply {
            yuvMat.get(0, 0, this)
        }

        val yData = yuvData.slice(0 until pixels).toByteArray()
        val uData = yuvData.slice(pixels until pixels + pixels / 4).toByteArray()
        val vData = yuvData.slice((pixels + pixels / 4) until pixels + pixels / 2).toByteArray()


        /* Convert separate Y, U and V values into N12 interleaved format */
        val uvData = run {
            val data = ByteArray(pixels / 2)
            for (i in 0 until data.size) {
                data[i] = if (i % 2 == 0) uData[i / 2] else vData[(i - 1) / 2]
            }
            data
        }

        val vuData = run {
            val data = ByteArray(pixels / 2)
            for (i in 0 until data.size) {
                data[i] = if (i % 2 == 0) vData[i / 2] else uData[(i - 1) / 2]
            }
            data
        }


        /* Create yuv object with n12 supported strides */
        yuv = Yuv(
            width = cols,
            height = rows,
            y = Yuv.Plane(
                buffer = ByteBuffer.wrap(yData),
                pixelStride = 1,
                rowStride = cols),
            u = Yuv.Plane(
                buffer = ByteBuffer.wrap(uvData),
                pixelStride = 2,
                rowStride = cols),
            v = Yuv.Plane(
                buffer = ByteBuffer.wrap(vuData),
                pixelStride = 2,
                rowStride = cols))


        yuvImage = YuvN12Image.create(yuv) ?: throw IllegalStateException("Wrong format")
    }


    @Test
    fun toRgb_matches_sourceRgb() {
        Assert.assertTrue(rgbSource.contentEquals(yuvImage.toRgb(), tolerance = 3.0))
    }

    @Test
    fun clipToRgb_matches_submatSourceRgb() {
        val clip = YuvImage.Clip(
            left = 20, top = 20, right = 50, bottom = 50)

        val clippedYuvImage = yuvImage with clip
        val clippedRgb = clippedYuvImage.toRgb()

        val clippedRgbSource = rgbSource.submat(
            clip.top, clip.bottom,
            clip.left, clip.right)


        Assert.assertTrue(clippedRgbSource.contentEquals(clippedRgb, tolerance = 3.0))
    }
}