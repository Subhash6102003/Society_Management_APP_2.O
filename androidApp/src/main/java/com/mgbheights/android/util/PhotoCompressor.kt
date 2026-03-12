package com.mgbheights.android.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.ImageView
import timber.log.Timber
import java.io.ByteArrayOutputStream

/**
 * Utility to compress photos and convert to/from Base64 data URI strings.
 * This allows storing photos directly in Firestore without Firebase Storage.
 *
 * Size estimates:
 * - Profile photo (300×300, 80% JPEG) → ~30-50KB → ~40-67KB Base64
 * - ID proof (800×600, 70% JPEG) → ~60-120KB → ~80-160KB Base64
 * - Total per user doc: well within Firestore's 1MB limit
 */
object PhotoCompressor {

    private const val DATA_URI_PREFIX = "data:image/jpeg;base64,"

    /**
     * Compress a profile photo: resize to 300×300 max, JPEG 80% quality.
     * Returns a data URI string: "data:image/jpeg;base64,..."
     */
    fun compressProfilePhoto(context: Context, uri: Uri): String? {
        return compressImage(context, uri, maxWidth = 300, maxHeight = 300, quality = 80)
    }

    /**
     * Compress an ID proof photo: resize to 800×600 max, JPEG 70% quality.
     * Returns a data URI string: "data:image/jpeg;base64,..."
     */
    fun compressIdProof(context: Context, uri: Uri): String? {
        return compressImage(context, uri, maxWidth = 800, maxHeight = 600, quality = 70)
    }

    /**
     * Core compression: decode → scale → compress → Base64 encode.
     */
    private fun compressImage(
        context: Context,
        uri: Uri,
        maxWidth: Int,
        maxHeight: Int,
        quality: Int
    ): String? = try {
        // Step 1: Decode bounds only to calculate sample size
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }

        // Step 2: Calculate inSampleSize for memory-efficient decoding
        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
        options.inJustDecodeBounds = false

        // Step 3: Decode the actual bitmap
        val bitmap = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: return null

        // Step 4: Scale to exact max dimensions while preserving aspect ratio
        val scaled = scaleBitmap(bitmap, maxWidth, maxHeight)
        if (scaled != bitmap) bitmap.recycle()

        // Step 5: Compress to JPEG
        val outputStream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        scaled.recycle()

        // Step 6: Base64 encode and wrap in data URI
        val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        val dataUri = "$DATA_URI_PREFIX$base64"

        Timber.d("Compressed photo: ${outputStream.size()} bytes, Base64 length: ${dataUri.length}")
        dataUri
    } catch (e: Exception) {
        Timber.e(e, "Failed to compress image")
        null
    }

    /**
     * Calculate an appropriate inSampleSize to avoid loading a huge bitmap into memory.
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height, width) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * Scale bitmap to fit within max dimensions while preserving aspect ratio.
     */
    private fun scaleBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) return bitmap

        val ratio = minOf(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Check if a string is a Base64 data URI.
     */
    fun isBase64DataUri(value: String?): Boolean {
        return value != null && value.startsWith(DATA_URI_PREFIX)
    }

    /**
     * Decode a Base64 data URI string to a Bitmap.
     */
    fun decodeBase64ToBitmap(dataUri: String): Bitmap? = try {
        val base64 = dataUri.removePrefix(DATA_URI_PREFIX)
        val bytes = Base64.decode(base64, Base64.NO_WRAP)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (e: Exception) {
        Timber.e(e, "Failed to decode Base64 image")
        null
    }

    /**
     * Load a photo into an ImageView. Handles Base64 data URIs and fallback placeholder.
     */
    fun loadPhotoIntoView(imageView: ImageView, photoUrl: String?, placeholderRes: Int) {
        if (photoUrl.isNullOrBlank()) {
            imageView.setImageResource(placeholderRes)
            return
        }

        if (isBase64DataUri(photoUrl)) {
            val bitmap = decodeBase64ToBitmap(photoUrl)
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
            } else {
                imageView.setImageResource(placeholderRes)
            }
        } else {
            // Regular URL — use Coil
            imageView.setImageResource(placeholderRes)
        }
    }
}

