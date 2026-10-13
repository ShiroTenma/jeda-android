package com.jeda.android.monitoring.visual

import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.media.Image
import android.media.ImageReader
import java.io.ByteArrayOutputStream

/**
 * Ephemeral frame processor. Converts raw ImageReader Image to a downscaled JPEG byte array
 * and releases memory immediately. Never writes to disk, cache, or database.
 */
object FrameProcessor {

    fun processImage(
        image: Image,
        targetWidth: Int = 224,
        targetHeight: Int = 224,
        quality: Int = 80
    ): ByteArray? {
        var bitmap: Bitmap? = null
        var scaledBitmap: Bitmap? = null
        try {
            val planes = image.planes
            val buffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * image.width

            val rawBitmap = Bitmap.createBitmap(
                image.width + rowPadding / pixelStride,
                image.height,
                Bitmap.Config.ARGB_8888
            )
            rawBitmap.copyPixelsFromBuffer(buffer)

            bitmap = if (rowPadding == 0) {
                rawBitmap
            } else {
                val cropped = Bitmap.createBitmap(rawBitmap, 0, 0, image.width, image.height)
                rawBitmap.recycle()
                cropped
            }

            scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            return outputStream.toByteArray()
        } catch (_: Exception) {
            return null
        } finally {
            scaledBitmap?.recycle()
            bitmap?.recycle()
            image.close()
        }
    }
}
