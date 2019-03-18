package com.quickbirdstudios.yuv2mat

import android.media.Image
import org.opencv.core.Mat
import android.graphics.ImageFormat

/*
################################################################################################
PUBLIC API
################################################################################################
*/

/**
 * Most convenient way of converting Yuv420_888 frames of Android into
 * an OpenCV RGB [Mat]
 *
 * @see ImageFormat.YUV_420_888
 */
fun Image.rgb(): Mat {
    return YuvImage(this).toRgb()
}