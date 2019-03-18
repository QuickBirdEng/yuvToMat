package com.quickbirdstudios.yuv2mat

import com.roche.greendot.pipeline.yuv.YuvPlanes

internal interface YuvImageFactory : YuvPlanes {
    fun create(yuv: Yuv): YuvImage?
}