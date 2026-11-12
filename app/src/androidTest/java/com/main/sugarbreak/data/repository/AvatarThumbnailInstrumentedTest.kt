package com.main.sugarbreak.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class AvatarThumbnailInstrumentedTest {
    @Test
    fun pickerImageIsCroppedResizedAndCompressed() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val input = File.createTempFile("avatar", ".png", context.cacheDir)
        try {
            val random = Random(42)
            val pixels = IntArray(640 * 320) { random.nextInt() or 0xff000000.toInt() }
            val source = Bitmap.createBitmap(pixels, 640, 320, Bitmap.Config.ARGB_8888)
            input.outputStream().use { assertTrue(source.compress(Bitmap.CompressFormat.PNG, 100, it)) }
            val jpeg = AvatarThumbnail.read(context.contentResolver, Uri.fromFile(input))
            assertTrue(jpeg.size in 1..MAX_AVATAR_BYTES)
            val thumbnail = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size)
            assertEquals(256, thumbnail.width)
            assertEquals(256, thumbnail.height)
        } finally {
            input.delete()
        }
    }

    @Test
    fun invalidPickerImageIsRejected() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val input = File.createTempFile("invalid-avatar", ".jpg", context.cacheDir)
        try {
            input.writeText("invalid image")
            assertTrue(runCatching {
                AvatarThumbnail.read(context.contentResolver, Uri.fromFile(input))
            }.isFailure)
        } finally {
            input.delete()
        }
    }
}
