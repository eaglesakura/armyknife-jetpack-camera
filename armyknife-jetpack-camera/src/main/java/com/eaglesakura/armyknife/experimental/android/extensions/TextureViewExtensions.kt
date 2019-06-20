package com.eaglesakura.armyknife.experimental.android.extensions

import android.graphics.Matrix
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import androidx.camera.core.Preview
import androidx.core.view.size

private const val ROUND_UP = 0.999999999

/**
 * Texture aspect ratio.
 */
val Preview.PreviewOutput.textureAspect: Double
    get() = textureSize.width.toDouble() / textureSize.height.toDouble()

/**
 * This method returns preview window size.
 * The area of returns value less than area of window.
 */
fun Preview.PreviewOutput.getPreviewSizeInWindow(windowWidth: Int, windowHeight: Int): Array<Int> {
    val width = (windowHeight.toDouble() * textureAspect + ROUND_UP).toInt()
    if (width <= windowWidth) {
        return arrayOf(width, windowHeight)
    }

    return arrayOf(windowWidth, (windowWidth.toDouble() / textureAspect + ROUND_UP).toInt())
}

/**
 * This method returns preview window size.
 * The area of returns value greater than area of window.
 */
fun Preview.PreviewOutput.getPreviewSizeWrapWindow(
    windowWidth: Int,
    windowHeight: Int
): Array<Int> {
    val sizeInWindow = getPreviewSizeInWindow(windowWidth, windowHeight)
    if (sizeInWindow[0] < windowWidth) {
        val scale = windowWidth.toDouble() / sizeInWindow[0].toDouble()
        sizeInWindow[0] = windowWidth
        sizeInWindow[1] = (sizeInWindow[1] * scale + ROUND_UP).toInt()
    } else {
        val scale = windowHeight.toDouble() / sizeInWindow[1].toDouble()
        sizeInWindow[0] = (sizeInWindow[0] * scale + ROUND_UP).toInt()
        sizeInWindow[1] = windowHeight
    }
    return sizeInWindow
}

/**
 * apply camera transform matrix to this View.
 *
 * e.g.)
 *  val preview = Preview(config).also { preview ->
 *      preview.setOnPreviewOutputUpdateListener {
 *          textureView.setTransform(it)
 *          textureView.surfaceTexture = it.surfaceTexture
 *      }
 *  }
 *  CameraX.bindToLifecycle(lifecycleOwner, preview)
 *
 *  TODO: should support in portrait preview.
 */
fun TextureView.setTransform(previewOutput: Preview.PreviewOutput) {
    val targetSize = previewOutput.getPreviewSizeWrapWindow(width, height)
    val matrix = Matrix()

    // Compute the center of the view finder
    val centerX = width / 2f
    val centerY = height / 2f

    // Correct preview output to account for display rotation
    val rotationDegrees = when (display.rotation) {
        Surface.ROTATION_0 -> 0
        Surface.ROTATION_90 -> 90
        Surface.ROTATION_180 -> 180
        Surface.ROTATION_270 -> 270
        else -> 0
    }
    matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)
    matrix.postScale(
        targetSize[0].toFloat() / width.toFloat(),
        targetSize[1].toFloat() / height.toFloat(),
        centerX, centerY
    )

    // Finally, apply transformations to our TextureView
    setTransform(matrix)
}

/**
 * Attach and setup camera preview.
 *
 * e.g.)
 * Preview(config).also { preview ->
 *      preview.setOnPreviewOutputUpdateListener {
 *          textureView.attach(it)
 *      }
 * }
 */
fun TextureView.attach(previewOutput: Preview.PreviewOutput) {
    val parent = parent as ViewGroup
    val params = layoutParams
    val index = let {
        for (i in 0 until parent.size) {
            if (parent.getChildAt(i) == this) {
                return@let i
            }
        }
        return@let 0
    }
    parent.removeView(this)
    parent.addView(this, index, params)
    surfaceTexture = previewOutput.surfaceTexture
    setTransform(previewOutput)
}