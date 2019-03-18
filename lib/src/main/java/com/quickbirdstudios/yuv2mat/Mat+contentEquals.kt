package com.quickbirdstudios.yuv2mat

import org.opencv.core.Mat
import kotlin.math.absoluteValue

/*
################################################################################################
INTERNAL API
################################################################################################
*/

internal fun Mat.contentEquals(other: Mat, tolerance: Double = 0.0): Boolean {
    if (this.rows() != other.rows()) return false
    if (this.cols() != other.cols()) return false
    if (this.channels() != other.channels()) return false

    this.forEach { row, col ->
        val thisData = this.get(row, col)
        val otherData = other.get(row, col)

        if (thisData.size != otherData.size) {
            return false
        }

        for (i in 0 until thisData.size) {
            val thisValue = thisData[i]
            val otherValue = otherData[i]
            if (thisValue.minus(otherValue).absoluteValue > tolerance) {
                return false
            }
        }
    }

    return true
}