package com.quickbirdstudios.yuv2mat

/*
################################################################################################
PUBLIC API
################################################################################################
*/

operator fun YuvImage.Clip.times(value: Int): YuvImage.Clip {
    return YuvImage.Clip(
        left = this.left * value,
        top = this.top * value,
        right = this.right * value,
        bottom = this.bottom * value)
}

operator fun YuvImage.Clip.div(value: Int): YuvImage.Clip {
    return YuvImage.Clip(
        left = this.left / value,
        top = this.top / value,
        right = this.right / value,
        bottom = this.bottom / value)
}