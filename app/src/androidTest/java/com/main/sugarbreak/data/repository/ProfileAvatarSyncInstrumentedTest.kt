package com.main.sugarbreak.data.repository

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.main.sugarbreak.data.firebase.FirebaseServices
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ProfileAvatarSyncInstrumentedTest {
    @Test
    fun avatarRestoresAndClearsOnAnIndependentAndroidClient() = runBlocking {
        // Run explicitly with the local Firebase Auth and Firestore emulators.
        assumeTrue(InstrumentationRegistry.getArguments().getString("avatarEmulators") == "true")
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val id = UUID.randomUUID().toString()
        val options = FirebaseOptions.Builder()
            .setProjectId("demo-sugarbreak-admin")
            .setApplicationId("1:1234567890:android:1234567890abcdef")
            .setApiKey("fake-api-key")
            .build()
        val first = FirebaseApp.initializeApp(context, options, "avatar-first-$id")
        val second = FirebaseApp.initializeApp(context, options, "avatar-second-$id")
        try {
            withTimeout(25_000) {
                suspend fun signIn(app: FirebaseApp): Pair<String, ProfileAvatarRepository> {
                    val auth = FirebaseAuth.getInstance(app).apply { useEmulator("10.0.2.2", 9099) }
                    val db = FirebaseFirestore.getInstance(app).apply { useEmulator("10.0.2.2", 8088) }
                    val token = """{"sub":"$id","email":"$id@example.test","email_verified":true}"""
                    val user = requireNotNull(auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await().user)
                    val services = FirebaseServices(auth, db)
                    return user.uid to ProfileAvatarRepository(services, FirebaseAuthenticationRepository(services, context))
                }

                val (uid, writer) = signIn(first)
                val photo = AvatarThumbnail.encode(Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888))
                writer.save(uid, photo)

                val (secondUid, reader) = signIn(second)
                assertEquals(uid, secondUid)
                val restored = reader.avatar.first { it.jpeg != null && !it.pending }
                assertArrayEquals(photo, restored.jpeg)

                writer.save(uid, null)
                val db = FirebaseFirestore.getInstance(second)
                val snapshot = db.collection("profileAvatars").document(uid)
                    .get(com.google.firebase.firestore.Source.SERVER).await()
                assertEquals(false, snapshot.exists())
            }
        } finally {
            first.delete()
            second.delete()
        }
    }
}
