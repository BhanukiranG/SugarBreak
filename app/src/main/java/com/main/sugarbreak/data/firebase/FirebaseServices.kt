package com.main.sugarbreak.data.firebase

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

data class FirebaseServices(
    val auth: FirebaseAuth?,
    val firestore: FirebaseFirestore?
)

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides
    @Singleton
    fun provideFirebaseServices(@ApplicationContext context: Context): FirebaseServices {
        val app = FirebaseApp.initializeApp(context)
        return FirebaseServices(
            auth = app?.let(FirebaseAuth::getInstance),
            firestore = app?.let(FirebaseFirestore::getInstance)
        )
    }
}
