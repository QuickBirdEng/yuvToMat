package com.quickbirdstudios.yuv2mat

import java.nio.ByteBuffer

/*
################################################################################################
INTERNAL API
################################################################################################
*/

internal fun ByteBuffer.toByteArray(): ByteArray {
    return ByteArray(limit()).apply {
        val source = duplicate()
        source.position(0)
        source.get(this)
    }
}