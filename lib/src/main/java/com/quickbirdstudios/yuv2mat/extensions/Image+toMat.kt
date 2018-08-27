@file:JvmName("Yuv")

package com.quickbirdstudios.yuv2mat.extensions

import android.media.Image
import com.quickbirdstudios.yuv2mat.Clip
import com.quickbirdstudios.yuv2mat.ImageConverter
import org.opencv.core.Mat

/*
################################################################################################
PUBLIC API
################################################################################################
*/

@JvmOverloads
fun Image.toMat(clip: Clip? = null): Mat {
    return (ImageConverter())(this, clip)
}