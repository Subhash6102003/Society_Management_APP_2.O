package com.mgbheights.android.util

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Utility for image compression and file operations.
 */
object ImageUtil {

    private const val MAX_WIDTH = 1024
    private const val MAX_HEIGHT = 1024
    private const val COMPRESSION_QUALITY = 80

    /**
     * Compress an image from a URI to a byte array.
     */
    fun compressImage(context: Context, uri: Uri, maxSizeKB: Int = 500): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val original = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            val scaledBitmap = scaleBitmap(original)
            val outputStream = ByteArrayOutputStream()

            var quality = COMPRESSION_QUALITY
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

            while (outputStream.toByteArray().size > maxSizeKB * 1024 && quality > 10) {
                outputStream.reset()
                quality -= 10
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            }

            if (scaledBitmap != original) {
                original.recycle()
            }

            outputStream.toByteArray()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Save a bitmap to a temporary file.
     */
    fun saveBitmapToFile(context: Context, bitmap: Bitmap, fileName: String): File {
        val file = File(context.cacheDir, fileName)
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, outputStream)
        outputStream.flush()
        outputStream.close()
        return file
    }

    /**
     * Scale a bitmap to fit within max dimensions while maintaining aspect ratio.
     */
    private fun scaleBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= MAX_WIDTH && height <= MAX_HEIGHT) return bitmap

        val ratio = minOf(
            MAX_WIDTH.toFloat() / width,
            MAX_HEIGHT.toFloat() / height
        )

        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Get file size in KB from a URI.
     */
    fun getFileSizeKB(context: Context, uri: Uri): Long {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            val sizeIndex = cursor?.getColumnIndex(android.provider.OpenableColumns.SIZE) ?: -1
            cursor?.moveToFirst()
            val size = cursor?.getLong(sizeIndex) ?: 0L
            cursor?.close()
            size / 1024
        } catch (e: Exception) {
            0L
        }
    }
}

