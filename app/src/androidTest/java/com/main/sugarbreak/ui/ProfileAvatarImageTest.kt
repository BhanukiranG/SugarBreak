package com.main.sugarbreak.ui

import android.graphics.Bitmap
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.main.sugarbreak.data.repository.AvatarThumbnail
import com.main.sugarbreak.ui.components.ProfileAvatarImage
import org.junit.Rule
import org.junit.Test

class ProfileAvatarImageTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun tappingPhotoOpensViewerAndCloseDismissesIt() {
        val jpeg = AvatarThumbnail.encode(Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888))
        compose.setContent {
            ProfileAvatarImage(jpeg, Modifier.size(48.dp), "Profile picture") {}
        }
        compose.waitUntil(5_000) {
            compose.onAllNodesWithContentDescription("Profile picture").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithContentDescription("Profile picture").performClick()
        compose.onNodeWithContentDescription("Full profile picture").assertExists()
        compose.onNodeWithContentDescription("Close profile picture").performClick()
        compose.onNodeWithContentDescription("Full profile picture").assertDoesNotExist()
    }
}
