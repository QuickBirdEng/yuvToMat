package com.quickbirdstudios.yuv2mat.internal

import android.media.Image
import com.quickbirdstudios.yuv2mat.Clip
import com.quickbirdstudios.yuv2mat.ImageConverter
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

/*
################################################################################################
INTERNAL API
################################################################################################
*/
internal class PreClipYuvToMatConverter : ImageConverter {
    override fun invoke(image: Image, clip: Clip?): Mat {


        val yuvImage = Yuv420888Image(image)

        val effectiveYuvImage = if (clip != null) yuvImage.clip(clip) else yuvImage

        val yuvMat = Mat(
            /*
            All three planes of the yuvMat are encoded into a single channel.
            This means, that the mat needs to fit all Y values all U values and all V values in it.
            Since there are just 1/4 U and V values than Y values we need to add 50% height
            to fit everything into the mat
            */
            effectiveYuvImage.height + effectiveYuvImage.height / 2,
            effectiveYuvImage.width,
            CvType.CV_8UC1,

            /*
            Directly passing the ByteBuffer into the matrix will also reduce
            copying of data
             */
            effectiveYuvImage.data)


        val effectiveRgb = Mat(
            effectiveYuvImage.height,
            effectiveYuvImage.width,
            CvType.CV_8UC3)

        /*
        The yuvMat is in form of YUV420sp. Converting to BGR resulted in
        proper rgb images. This option seems weird, but we tested resulting mat objects
        and they are properly created in RGB order.

        Tests were done by using the .toBitmap function which explicitly asks for RGB images
        and by taking images of red objects and checking the position of the R signal.
         */
        Imgproc.cvtColor(yuvMat, effectiveRgb, Imgproc.COLOR_YUV420sp2BGR, 3)

        return effectiveRgb
    }
}