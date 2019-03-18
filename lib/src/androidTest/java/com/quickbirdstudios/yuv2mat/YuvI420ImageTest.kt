package com.quickbirdstudios.yuv2mat


import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.opencv.android.OpenCVLoader
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.nio.ByteBuffer

/**
 * This test will use openCV to fabricate a synthetic yuv image in the
 * I410 format.
 * The image will be compared to the source rgb to check whether or not
 * conversion works as expected.
 */
class YuvI420ImageTest {

    private lateinit var rgbSource: Mat
    private lateinit var yuv: Yuv
    private lateinit var yuvImage: YuvImage

    @Before
    fun setup() {
        OpenCVLoader.initDebug()

        /* Arbitrary row and col count */
        val rows = 100
        val cols = 100
        val pixels = rows * cols

        /* Create arbitrary rgb source mat */
        rgbSource = Mat(rows, cols, CvType.CV_8UC3).forEach { row, col ->
            val r = row.toByte()
            val g = col.toByte()
            val b = (r + g / 2).toByte()
            put(row, col, byteArrayOf(r, g, b))
        }

        /* Synthesize yuv data */
        val yuvMat = Mat(rows + rows / 2, cols, CvType.CV_8UC1).also { yuvMat ->
            Imgproc.cvtColor(rgbSource, yuvMat, Imgproc.COLOR_RGB2YUV_I420)
        }

        val yuvData = ByteArray(pixels + pixels / 2).apply {
            yuvMat.get(0, 0, this)
        }

        val yData = yuvData.slice(0 until pixels).toByteArray()
        val uData = yuvData.slice(pixels until pixels + pixels / 4).toByteArray()
        val vData = yuvData.slice((pixels + pixels / 4) until pixels + pixels / 2).toByteArray()


        /* Create yuv object with correct strides from synthesized yuv data */
        yuv = Yuv(
            width = cols,
            height = rows,
            y = Yuv.Plane(
                buffer = ByteBuffer.wrap(yData),
                pixelStride = 1,
                rowStride = cols),
            u = Yuv.Plane(
                buffer = ByteBuffer.wrap(uData),
                pixelStride = 1,
                rowStride = cols / 2),
            v = Yuv.Plane(
                buffer = ByteBuffer.wrap(vData),
                pixelStride = 1,
                rowStride = cols / 2))


        yuvImage = YuvI420Image.create(yuv) ?: throw IllegalStateException("Wrong format")
    }


    @Test
    fun toRgb_matches_sourceRgb() {
        assertTrue(rgbSource.contentEquals(yuvImage.toRgb(), tolerance = 3.0))
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


        assertTrue(clippedRgbSource.contentEquals(clippedRgb, tolerance = 3.0))
    }
}