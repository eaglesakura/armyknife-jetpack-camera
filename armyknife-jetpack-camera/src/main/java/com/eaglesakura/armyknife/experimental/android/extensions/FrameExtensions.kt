package com.eaglesakura.armyknife.experimental.android.extensions

import android.graphics.ImageFormat
import androidx.camera.core.ImageProxy
import com.google.android.gms.vision.Frame
import java.nio.ByteBuffer

private fun convertYUV420888ToNV21(proxy: ImageProxy): ByteBuffer {
    val buffer0 = if (proxy.planes.isEmpty()) {
        throw IllegalArgumentException("plane size error")
    } else {
        proxy.planes[0]
    }.buffer
    val buffer2 = if (proxy.planes.size < 3) {
        throw IllegalArgumentException("plane size error")
    } else {
        proxy.planes[2]
    }.buffer

    val dst = ByteBuffer.allocate(buffer0.limit() + buffer2.limit())
    dst.position(0)
    dst.put(buffer0)
    dst.put(buffer2)
    dst.position(0)
    return dst
}

/**
 * setup Frame builder from Jetpack's ImagProxy.
 *
 * Supported ImageFormat is [YUV_420_888 | NV16 | NV21],
 * or Mobile VISION api supported format.
 *
 * @see ImageFormat.YUV_420_888
 * @see ImageFormat.NV16
 * @see ImageFormat.NV21
 */
fun Frame.Builder.setImageData(image: ImageProxy): Frame.Builder {
    when (image.format) {
        ImageFormat.YUV_420_888 -> {
            return setImageData(
                convertYUV420888ToNV21(
                    image
                ),
                image.width,
                image.height,
                ImageFormat.NV21
            )
        }
        else -> return setImageData(
            image.planes[0].buffer,
            image.width,
            image.height,
            image.format
        )
    }
}