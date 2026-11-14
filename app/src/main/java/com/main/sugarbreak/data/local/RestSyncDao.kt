package com.main.sugarbreak.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "rest_sync_state")
data class RestSyncState(@PrimaryKey val uid: String, val state: String)

@Dao
interface RestSyncDao {
    @Query("SELECT * FROM rest_sync_state WHERE uid = :uid")
    suspend fun read(uid: String): RestSyncState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(state: RestSyncState)
}
