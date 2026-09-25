package com.main.nosugar.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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
        val ONBOARDING_COMPLETED = booleanPreferencesKey("ONBOARDING_COMPLETED")
        val CHALLENGE_BEHAVIOR = stringPreferencesKey("CHALLENGE_BEHAVIOR")
        val USER_NAME = stringPreferencesKey("USER_NAME")
    }

    val reminderEnabled: Flow<Boolean> = context.dataStore.data.map { it[REMINDER_ENABLED] ?: false }
    val reminderHour: Flow<Int> = context.dataStore.data.map { it[REMINDER_HOUR] ?: 20 }
    val reminderMinute: Flow<Int> = context.dataStore.data.map { it[REMINDER_MINUTE] ?: 0 }
    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { it[ONBOARDING_COMPLETED] ?: false }
    val challengeBehavior: Flow<String?> = context.dataStore.data.map { it[CHALLENGE_BEHAVIOR] }
    val userName: Flow<String?> = context.dataStore.data.map { it[USER_NAME] }

    suspend fun setReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { it[REMINDER_ENABLED] = enabled }
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit {
            it[REMINDER_HOUR] = hour
            it[REMINDER_MINUTE] = minute
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setChallengeBehavior(behavior: String) {
        context.dataStore.edit { it[CHALLENGE_BEHAVIOR] = behavior }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { it[USER_NAME] = name }
    }
}
