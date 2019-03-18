package com.quickbirdstudios.yuv2mat

/*
################################################################################################
INTERNAL API
################################################################################################
*/

internal interface YuvImageFactory : YuvPlanes {
    fun create(yuv: Yuv): YuvImage?
}