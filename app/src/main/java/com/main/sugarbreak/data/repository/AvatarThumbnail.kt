package com.main.sugarbreak.data.repository

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import java.io.ByteArrayOutputStream

internal object AvatarThumbnail {
    private const val SIZE = 256

    fun read(resolver: ContentResolver, uri: Uri): ByteArray {
        val bitmap = if (Build.VERSION.SDK_INT >= 28) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.setTargetSampleSize(sampleSize(info.size.width, info.size.height))
            }
        } else {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, options) }
            require(options.outWidth > 0 && options.outHeight > 0)
            options.inSampleSize = sampleSize(options.outWidth, options.outHeight)
            options.inJustDecodeBounds = false
            val decoded = requireNotNull(resolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, options) })
            val orientation = resolver.openInputStream(uri)?.use { stream ->
                runCatching { ExifInterface(stream).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1) }.getOrDefault(1)
            } ?: 1
            orient(decoded, orientation)
        }
        return encode(bitmap)
    }

    private fun sampleSize(width: Int, height: Int): Int {
        var sample = 1
        while (maxOf(width, height) / sample > 1024) sample *= 2
        return sample
    }

    private fun orient(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            2 -> matrix.setScale(-1f, 1f)
            3 -> matrix.setRotate(180f)
            4 -> matrix.setScale(1f, -1f)
            5 -> { matrix.setRotate(90f); matrix.postScale(-1f, 1f) }
            6 -> matrix.setRotate(90f)
            7 -> { matrix.setRotate(-90f); matrix.postScale(-1f, 1f) }
            8 -> matrix.setRotate(-90f)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    internal fun encode(bitmap: Bitmap): ByteArray {
        val side = minOf(bitmap.width, bitmap.height)
        val square = Bitmap.createBitmap(bitmap, (bitmap.width - side) / 2, (bitmap.height - side) / 2, side, side)
        val thumbnail = Bitmap.createScaledBitmap(square, SIZE, SIZE, true)
        val output = ByteArrayOutputStream()
        for (quality in 90 downTo 10 step 10) {
            output.reset()
            check(thumbnail.compress(Bitmap.CompressFormat.JPEG, quality, output))
            if (output.size() <= MAX_AVATAR_BYTES) return output.toByteArray()
        }
        error("The picture could not be compressed")
    }
}
