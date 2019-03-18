package com.quickbirdstudios.yuv2mat


/*
################################################################################################
INTERNAL API
################################################################################################
*/

/**
 * Adjusts the clip to the bounds of the 4x4 pixel grid of the yuv 420 format.
 * Arbitrary clipping is not allowed, since u and v values are sub-sampled in 4x4 pixel blocks.
 */
internal fun YuvImage.Clip.adjustToYuvGrid(): YuvImage.Clip {
    fun Int.isInGrid() = this % 2 == 0
    return YuvImage.Clip(
        left = if (this.left.isInGrid()) this.left else this.left - 1,
        top = if (this.top.isInGrid()) this.top else this.top - 1,
        right = if (this.right.isInGrid()) this.right else right + 1,
        bottom = if (this.bottom.isInGrid()) this.bottom else this.bottom + 1)
}