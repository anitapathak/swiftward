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
        private val KEY_EMAIL    = stringPreferencesKey("user_email")   // NEW: store email for payment emails
    }

    val token: Flow<String?>    = context.dataStore.data.map { it[KEY_TOKEN] }
    val userName: Flow<String?> = context.dataStore.data.map { it[KEY_USERNAME] }
    val userPhone: Flow<String?> = context.dataStore.data.map { it[KEY_PHONE] }
    val userEmail: Flow<String?> = context.dataStore.data.map { it[KEY_EMAIL] }
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { it[KEY_TOKEN] != null }

    suspend fun saveSession(token: String, userId: String, name: String, phone: String, email: String = "") {
        context.dataStore.edit {
            it[KEY_TOKEN]    = token
            it[KEY_USER_ID]  = userId
            it[KEY_USERNAME] = name
            it[KEY_PHONE]    = phone
            it[KEY_EMAIL]    = email
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun getToken(): String? =
        context.dataStore.data.map { it[KEY_TOKEN] }.firstOrNull()

    suspend fun getEmail(): String? =
        context.dataStore.data.map { it[KEY_EMAIL] }.firstOrNull()
}