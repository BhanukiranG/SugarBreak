package com.main.sugarbreak.domain.repository

interface ProgressSyncRepository {
    suspend fun syncNow(uid: String)
    fun schedule(uid: String)
}
