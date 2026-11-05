package com.main.sugarbreak.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {
    companion object {
        val REMINDER_ENABLED = booleanPreferencesKey("REMINDER_ENABLED")
        val REMINDER_HOUR = intPreferencesKey("REMINDER_HOUR")
        val REMINDER_MINUTE = intPreferencesKey("REMINDER_MINUTE")
        private val LEGACY_ONBOARDING_COMPLETED = booleanPreferencesKey("ONBOARDING_COMPLETED")
        private val LEGACY_CHALLENGE_BEHAVIOR = stringPreferencesKey("CHALLENGE_BEHAVIOR")
        val USER_NAME = stringPreferencesKey("USER_NAME")
        val IS_DARK_MODE = booleanPreferencesKey("IS_DARK_MODE")
        val APP_THEME = stringPreferencesKey("APP_THEME")
    }

    val reminderEnabled: Flow<Boolean> = context.dataStore.data.map { it[REMINDER_ENABLED] ?: false }
    val reminderHour: Flow<Int> = context.dataStore.data.map { it[REMINDER_HOUR] ?: 20 }
    val reminderMinute: Flow<Int> = context.dataStore.data.map { it[REMINDER_MINUTE] ?: 0 }
    val isDarkMode: Flow<Boolean?> = context.dataStore.data.map { it[IS_DARK_MODE] }
    val appTheme: Flow<String?> = context.dataStore.data.map { it[APP_THEME] }

    suspend fun setReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { it[REMINDER_ENABLED] = enabled }
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit {
            it[REMINDER_HOUR] = hour
            it[REMINDER_MINUTE] = minute
        }
    }

    fun onboardingCompleted(uid: String?): Flow<Boolean?> = context.dataStore.data.map { preferences ->
        uid?.let { preferences[onboardingKey(it)] }
    }

    suspend fun setOnboardingCompleted(uid: String, completed: Boolean) {
        context.dataStore.edit { it[onboardingKey(uid)] = completed }
    }

    suspend fun claimLegacyOnboarding(uid: String) {
        context.dataStore.edit { preferences ->
            val accountKey = onboardingKey(uid)
            val claimedValue = claimLegacyOnboardingValue(
                accountValue = preferences[accountKey],
                legacyValue = preferences[LEGACY_ONBOARDING_COMPLETED]
            )
            if (preferences[accountKey] == null && claimedValue != null) preferences[accountKey] = claimedValue
            preferences.remove(LEGACY_ONBOARDING_COMPLETED)
        }
    }

    fun challengeBehavior(uid: String?): Flow<StoredChallengeBehavior?> = context.dataStore.data.map { preferences ->
        uid?.let { accountUid ->
            val value = preferences[challengeBehaviorKey(accountUid)] ?: return@let null
            StoredChallengeBehavior(
                value = value,
                updatedAt = preferences[challengeBehaviorUpdatedAtKey(accountUid)] ?: 0L
            )
        }
    }

    suspend fun setChallengeBehavior(uid: String, behavior: String, updatedAt: Long = System.currentTimeMillis()) {
        context.dataStore.edit {
            it[challengeBehaviorKey(uid)] = behavior
            it[challengeBehaviorUpdatedAtKey(uid)] = updatedAt
        }
    }

    suspend fun claimLegacyChallengeBehavior(uid: String) {
        context.dataStore.edit { preferences ->
            val accountKey = challengeBehaviorKey(uid)
            if (preferences[accountKey] == null) {
                preferences[LEGACY_CHALLENGE_BEHAVIOR]?.let { legacy ->
                    preferences[accountKey] = legacy
                    preferences[challengeBehaviorUpdatedAtKey(uid)] = 0L
                }
            }
            preferences.remove(LEGACY_CHALLENGE_BEHAVIOR)
        }
    }

    fun userName(uid: String?): Flow<StoredProfileName?> = context.dataStore.data.map { preferences ->
        uid?.let { accountUid ->
            val value = preferences[userNameKey(accountUid)] ?: return@let null
            StoredProfileName(
                value = value,
                updatedAt = preferences[userNameUpdatedAtKey(accountUid)] ?: 0L
            )
        }
    }

    suspend fun setUserName(uid: String, name: String, updatedAt: Long = System.currentTimeMillis()) {
        context.dataStore.edit {
            it[userNameKey(uid)] = name
            it[userNameUpdatedAtKey(uid)] = updatedAt
        }
    }

    suspend fun claimLegacyUserName(uid: String) {
        context.dataStore.edit { preferences ->
            val accountKey = userNameKey(uid)
            if (preferences[accountKey] == null && preferences[USER_NAME] != null) {
                preferences[accountKey] = preferences[USER_NAME]!!
                preferences[userNameUpdatedAtKey(uid)] = 0L
            }
            preferences.remove(USER_NAME)
        }
    }

    suspend fun setIsDarkMode(isDark: Boolean?) {
        context.dataStore.edit { preferences ->
            if (isDark == null) preferences.remove(IS_DARK_MODE) else preferences[IS_DARK_MODE] = isDark
        }
    }

    suspend fun setAppTheme(theme: String) {
        context.dataStore.edit { it[APP_THEME] = theme }
    }

    private fun onboardingKey(uid: String) = booleanPreferencesKey("ONBOARDING_COMPLETED_$uid")
    private fun challengeBehaviorKey(uid: String) = stringPreferencesKey("CHALLENGE_BEHAVIOR_$uid")
    private fun challengeBehaviorUpdatedAtKey(uid: String) = longPreferencesKey("CHALLENGE_BEHAVIOR_UPDATED_AT_$uid")
    private fun userNameKey(uid: String) = stringPreferencesKey("USER_NAME_$uid")
    private fun userNameUpdatedAtKey(uid: String) = longPreferencesKey("USER_NAME_UPDATED_AT_$uid")
}

data class StoredChallengeBehavior(val value: String, val updatedAt: Long)
data class StoredProfileName(val value: String, val updatedAt: Long)

internal fun claimLegacyOnboardingValue(accountValue: Boolean?, legacyValue: Boolean?): Boolean? =
    accountValue ?: legacyValue
