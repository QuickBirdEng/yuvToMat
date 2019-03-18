package com.quickbirdstudios.yuv2mat

import java.nio.ByteBuffer

/*
################################################################################################
INTERNAL API
################################################################################################
*/

internal fun byteBuffer(direct: Boolean, capacity: Int): ByteBuffer {
    return if (direct) ByteBuffer.allocateDirect(capacity)
    else ByteBuffer.allocate(capacity)
}