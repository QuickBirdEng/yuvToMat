package com.quickbirdstudios.yuv2mat

import android.media.Image
import com.quickbirdstudios.yuv2mat.internal.PreClipYuvToMatConverter
import org.opencv.core.Mat

/*
################################################################################################
PUBLIC API
################################################################################################
*/

interface ImageConverter {
    operator fun invoke(image: Image, clip: Clip? = null): Mat

    companion object {
        @JvmName("create")
        operator fun invoke(): ImageConverter {
            return PreClipYuvToMatConverter()
        }
    }
}



