package com.quickbirdstudios.yuv2mat.extensions

import android.media.Image
import com.quickbirdstudios.yuv2mat.Clip
import com.quickbirdstudios.yuv2mat.ImageConverter
import com.quickbirdstudios.yuv2mat.invoke
import org.opencv.core.Mat

/*
################################################################################################
PUBLIC API
################################################################################################
*/

fun Image.toMat(clip: Clip? = null): Mat {
    return (ImageConverter())(this, clip)
}