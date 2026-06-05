package com.swiftward.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "swiftward_prefs")

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_TOKEN    = stringPreferencesKey("auth_token")
        private val KEY_USER_ID  = stringPreferencesKey("user_id")
        private val KEY_USERNAME = stringPreferencesKey("user_name")
        private val KEY_PHONE    = stringPreferencesKey("user_phone")
    }

    val token: Flow<String?> = context.dataStore.data.map { it[KEY_TOKEN] }
    val userName: Flow<String?> = context.dataStore.data.map { it[KEY_USERNAME] }
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { it[KEY_TOKEN] != null }

    suspend fun saveSession(token: String, userId: String, name: String, phone: String) {
        context.dataStore.edit {
            it[KEY_TOKEN]    = token
            it[KEY_USER_ID]  = userId
            it[KEY_USERNAME] = name
            it[KEY_PHONE]    = phone
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }

    // ✅ Fixed: uses firstOrNull() instead of GlobalScope hack
    suspend fun getToken(): String? =
        context.dataStore.data.map { it[KEY_TOKEN] }.firstOrNull()
}