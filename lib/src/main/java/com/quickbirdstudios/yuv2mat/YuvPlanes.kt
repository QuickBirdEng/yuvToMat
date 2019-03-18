package com.roche.greendot.pipeline.yuv

import android.media.Image

internal interface YuvPlanes {
    val Array<out Image.Plane>.y get() = this[0]
    val Array<out Image.Plane>.u get() = this[1]
    val Array<out Image.Plane>.v get() = this[2]
    companion object : YuvPlanes
}