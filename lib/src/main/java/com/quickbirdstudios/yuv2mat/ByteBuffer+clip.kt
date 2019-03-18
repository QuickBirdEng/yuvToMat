package com.quickbirdstudios.yuv2mat

import java.nio.ByteBuffer

/*
################################################################################################
PUBLIC API
################################################################################################
*/

fun ByteBuffer.clip(range: IntRange): ByteBuffer {
    val duplicate = this.duplicate()
    duplicate.position(range.start)
    duplicate.limit(range.endInclusive + 1)
    return duplicate.slice()
}