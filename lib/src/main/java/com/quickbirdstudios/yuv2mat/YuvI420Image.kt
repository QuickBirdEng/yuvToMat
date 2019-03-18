package com.quickbirdstudios.yuv2mat

import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.nio.ByteBuffer

/*
################################################################################################
INTERNAL API
################################################################################################
*/

/**
 * Planar image format with all y values in a row followed by all u values in a row
 * followed by al v values in a row:
 *
 * ```
 * y y y y y y
 * y y y y y y
 * y y y y y y
 * y y y y y y
 * u u u u u u
 * v v v v v v
 * ```
 *
 * This implementation expects [yuv] to
 * have a pixel stride of 1 for all planes
 * as well as no padding suggested by the row stride.
 *
 * One can use [defrag] to bring yuv data into this format
 *
 * see [http://www.fourcc.org/pixel-format/yuv-i420/] for more information
 */
internal class YuvI420Image private constructor(private val yuv: Yuv) : YuvImage, YuvPlanes {

    override fun with(clip: YuvImage.Clip): YuvImage {
        /* yClip: Clipping has to be adjusted to the 4x4 sub-sampled yuv grid*/
        val yClip = clip.adjustToYuvGrid()

        // U/V space has half the rows and half the columns
        val uClip = yClip / 2
        val vClip = yClip / 2



        val yMatrix = BufferMatrix(
            data = yuv.y.buffer, width = yuv.width, height = yuv.height)

        /* U/V only has half the rows and half the columns*/
        val uMatrix = BufferMatrix(
            data = yuv.u.buffer, width = yuv.width / 2, height = yuv.height / 2)
        val vMatrix = BufferMatrix(
            data = yuv.v.buffer, width = yuv.width / 2, height = yuv.height / 2)


        /* Clip the raw data */
        val yMatrixClipped = yMatrix with yClip
        val uMatrixClipped = uMatrix with uClip
        val vMatrixClipped = vMatrix with vClip


        /* Create image with correct strides */
        return YuvI420Image(
            Yuv(
                resource = yuv,
                width = yClip.width.value,
                height = yClip.height.value,
                y = Yuv.Plane(
                    buffer = yMatrixClipped.data,
                    pixelStride = 1,
                    rowStride = yClip.width.value),
                u = Yuv.Plane(
                    buffer = uMatrixClipped.data,
                    pixelStride = 1,
                    rowStride = yClip.width.value / 2),
                v = Yuv.Plane(
                    buffer = vMatrixClipped.data,
                    pixelStride = 1,
                    rowStride = yClip.width.value / 2)))
    }

    override fun toRgb(): Mat {

        /*
        Put all data into one ByteBuffer
         */

        val y = yuv.y.buffer
        val u = yuv.u.buffer
        val v = yuv.v.buffer

        y.position(0)
        u.position(0)
        v.position(0)

        val data = ByteBuffer.allocateDirect(
            y.remaining() + u.remaining() + v.remaining())

        data.put(y)
        data.put(u)
        data.put(v)


        /*
        Load the data into a mat.
        Since we have 1/4th the amount of u and v values than we have y values,
        the total height of the mat has to be the image height + 1/2 * image height
         */
        val yuvMat = Mat(yuv.rows + yuv.rows / 2, yuv.cols, CvType.CV_8UC1, data)


        /*
        Create the rgb mat with correct dimensions and type and
        use Improc to convert the color space
         */
        val rgbMat = Mat(yuv.rows, yuv.cols, CvType.CV_8UC3)
        Imgproc.cvtColor(yuvMat, rgbMat, Imgproc.COLOR_YUV2RGB_I420)


        yuvMat.release()
        return rgbMat
    }

    override fun close() = yuv.close()

    companion object Factory : YuvImageFactory {
        override fun create(yuv: Yuv): YuvImage? {
            if (
            /* Y plane must be padding free */
                yuv.y.rowStride == yuv.width &&

                /* Y plane must be compact */
                yuv.y.pixelStride == 1 &&

                /* U plane must be compact */
                yuv.u.pixelStride == 1 &&

                /* U plane must be padding free */
                yuv.u.rowStride == yuv.width / 2 &&

                /* V plane must be compact */
                yuv.v.pixelStride == 1 &&

                /* V plane must be padding free */
                yuv.v.rowStride == yuv.width / 2) {
                return YuvI420Image(yuv)
            }

            return null
        }
    }
}