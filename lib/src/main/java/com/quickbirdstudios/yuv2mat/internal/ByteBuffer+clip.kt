package com.quickbirdstudios.yuv2mat.internal

import java.nio.ByteBuffer

/*
################################################################################################
INTERNAL API
################################################################################################
*/

internal fun ByteBuffer.clip(range: IntRange): ByteBuffer {
    val duplicate = this.duplicate()
    duplicate.position(range.start)
    duplicate.limit(range.endInclusive + 1)
    return duplicate.slice()
}