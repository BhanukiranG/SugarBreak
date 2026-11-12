package com.main.sugarbreak.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ProfileAvatarImage(
    jpeg: ByteArray?,
    modifier: Modifier = Modifier,
    description: String? = null,
    onPlaceholderClick: (() -> Unit)? = null,
    placeholder: @Composable () -> Unit
) {
    key(jpeg) {
        var showPhoto by remember { mutableStateOf(false) }
        val image by produceState<ImageBitmap?>(null) {
            if (jpeg != null) {
                value = withContext(Dispatchers.Default) {
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size, bounds)
                    if (bounds.outWidth != 256 || bounds.outHeight != 256) null
                    else BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size)?.asImageBitmap()
                }
            }
        }
        val bitmap = image
        val interaction = when {
            bitmap != null -> Modifier.clickable(
                role = Role.Button,
                onClickLabel = "View profile picture"
            ) { showPhoto = true }
            onPlaceholderClick != null -> Modifier.clickable(
                role = Role.Button,
                onClick = onPlaceholderClick
            )
            else -> Modifier
        }
        Box(modifier.clip(CircleShape).then(interaction), contentAlignment = Alignment.Center) {
            if (bitmap == null) placeholder()
            else Image(bitmap, description, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
        if (showPhoto && bitmap != null) {
            Dialog(
                onDismissRequest = { showPhoto = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(Modifier.fillMaxSize().background(Color.Black).safeDrawingPadding()) {
                    Image(bitmap, "Full profile picture", Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                    IconButton(onClick = { showPhoto = false }, modifier = Modifier.align(Alignment.TopEnd)) {
                        Icon(Icons.Default.Close, "Close profile picture", tint = Color.White)
                    }
                }
            }
        }
    }
}
