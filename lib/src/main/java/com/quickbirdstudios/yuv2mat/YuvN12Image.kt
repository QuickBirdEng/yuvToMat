package com.roche.greendot.pipeline.yuv

import com.quickbirdstudios.yuv2mat.*
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
 * Semi-Planar image format with all y values in a row followed by interleaved u/v values
 *
 * ```
 * y y y y y y
 * y y y y y y
 * y y y y y y
 * y y y y y y
 * u v u v u v
 * v v v v u v
 * ``
 *
 * ### Android
 * This implementation exploits the fact that android devices
 * that report a pixel stride of 2 in the u and v plane
 * have the u plane interleaved with the v plane.
 *
 * That means, that the y plane with an appended u plane gives the
 * YuvN12 Format described above
 *
 *
 * see [http://www.fourcc.org/pixel-format/yuv-nv12/] for more information
 */
internal class YuvN12Image private constructor(private val yuv: Yuv) : YuvImage, YuvPlanes {

    override fun with(clip: YuvImage.Clip): YuvImage {
        /* Adjust clip to the 4x4 sub-sampled yuv 420 grid */
        val yClip = clip.adjustToYuvGrid()

        /* U/V Plane has the same width as the yPlane but just half the height */
        val uvClip = yClip.height / 2
        val yMatrix = BufferMatrix(yuv.y.buffer, width = yuv.width, height = yuv.height)
        val uvMatrix = BufferMatrix(yuv.u.buffer, width = yuv.width, height = yuv.height / 2)
        val yMatrixClipped = yMatrix with yClip
        val uvMatrixClipped = uvMatrix with uvClip

        /* Create image with correct strides */
        return YuvN12Image(
            Yuv(
                resource = yuv,
                width = yClip.width.value,
                height = yClip.height.value,
                y = Yuv.Plane(
                    buffer = yMatrixClipped.data,
                    pixelStride = 1,
                    rowStride = yClip.width.value),
                u = Yuv.Plane(
                    buffer = uvMatrixClipped.data,
                    pixelStride = 2,
                    rowStride = yClip.width.value),

                /* V Plane can be ignored and does not need to be clipped */
                v = Yuv.Plane(
                    buffer = ByteBuffer.allocate(0),
                    pixelStride = 2,
                    rowStride = yClip.width.value)))
    }

    override fun toRgb(): Mat {

        /*
        Load Y and U plane into one buffer.
        The V plane can be ignored, since the U plane contains all information
        of the V plane in its interleaved format (u1 v1 u2 v2, ...)
         */

        val y = yuv.y.buffer
        val u = yuv.u.buffer

        y.position(0)
        u.position(0)

        val data = java.nio.ByteBuffer.allocateDirect(y.remaining() + u.remaining())
        data.put(y)
        data.put(u)
        data.position(0)

        /*
        Load the data into a mat.
        Since we have 1/4th the amount of u and v values than we have y values,
        the total height of the mat has to be the image height + 1/2 * image height
        */
        val yuvMat = Mat(yuv.rows + yuv.rows / 2, yuv.width, CvType.CV_8UC1, data)


        /*
        Create the rgb mat with correct dimensions and type and
        use Improc to convert the color space
        */
        val rgbMat = Mat(yuv.rows, yuv.cols, CvType.CV_8UC3)
        Imgproc.cvtColor(yuvMat, rgbMat, Imgproc.COLOR_YUV2RGB_NV12)


        yuvMat.release()
        return rgbMat
    }

    override fun close() {
        yuv.close()
    }


    companion object Factory : YuvImageFactory {
        override fun create(yuv: Yuv): YuvImage? {
            if (
                yuv.height % 2 == 0 &&
                yuv.width % 2 == 0 &&
                yuv.y.pixelStride == 1 &&
                yuv.y.rowStride == yuv.width &&
                yuv.u.pixelStride == 2 &&
                yuv.u.rowStride == yuv.width &&
                yuv.v.pixelStride == 2 &&
                yuv.v.rowStride == yuv.width) {
                return YuvN12Image(yuv)
            }

            return null
        }
    }
}