package com.quickbirdstudios.yuv2mat

import org.opencv.core.Mat

/*
################################################################################################
INTERNAL API
################################################################################################
*/

internal inline fun Mat.forEach(block: Mat.(row: Int, col: Int) -> Unit): Mat {
    for (row in 0 until rows()) {
        for (col in 0 until cols()) {
            block(row, col)
        }
    }

    return this
}