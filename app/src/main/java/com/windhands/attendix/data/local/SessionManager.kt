package com.windhands.attendix.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val KEY_USER_ID = intPreferencesKey("user_id")
        private val KEY_EMPLOYEE_ID = stringPreferencesKey("employee_id")
        private val KEY_FIRST_NAME = stringPreferencesKey("first_name")
        private val KEY_LAST_NAME = stringPreferencesKey("last_name")
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_PHONE = stringPreferencesKey("phone")
        private val KEY_ROLE = stringPreferencesKey("role")
        private val KEY_DEPARTMENT = stringPreferencesKey("department")
        private val KEY_DESIGNATION = stringPreferencesKey("designation")
        private val KEY_STATUS = stringPreferencesKey("status")
        private val KEY_JOINING_DATE = stringPreferencesKey("joining_date")
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val KEY_REMEMBER_ME = booleanPreferencesKey("remember_me")
    }

    val authToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_AUTH_TOKEN]
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_IS_LOGGED_IN] ?: false
    }

    val userId: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_ID]
    }

    val employeeId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_EMPLOYEE_ID]
    }

    val firstName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_FIRST_NAME]
    }

    val lastName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_LAST_NAME]
    }

    val email: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_EMAIL]
    }

    val phone: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_PHONE]
    }

    val role: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_ROLE]
    }

    val department: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_DEPARTMENT]
    }

    val designation: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_DESIGNATION]
    }

    val status: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_STATUS]
    }

    val joiningDate: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_JOINING_DATE]
    }

    val fullName: Flow<String> = context.dataStore.data.map { preferences ->
        val firstName = preferences[KEY_FIRST_NAME] ?: ""
        val lastName = preferences[KEY_LAST_NAME] ?: ""
        "$firstName $lastName".trim()
    }

    val isAdmin: Flow<Boolean> = context.dataStore.data.map { preferences ->
        val role = preferences[KEY_ROLE]?.lowercase()
        role == "admin"
    }

    suspend fun saveSession(
        token: String,
        userId: Int,
        employeeId: String,
        firstName: String,
        lastName: String,
        email: String,
        phone: String?,
        role: String,
        department: String?,
        designation: String?,
        status: String,
        joiningDate: String?,
        rememberMe: Boolean
    ) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AUTH_TOKEN] = token
            preferences[KEY_USER_ID] = userId
            preferences[KEY_EMPLOYEE_ID] = employeeId
            preferences[KEY_FIRST_NAME] = firstName
            preferences[KEY_LAST_NAME] = lastName
            preferences[KEY_EMAIL] = email
            phone?.let { preferences[KEY_PHONE] = it }
            preferences[KEY_ROLE] = role
            department?.let { preferences[KEY_DEPARTMENT] = it }
            designation?.let { preferences[KEY_DESIGNATION] = it }
            preferences[KEY_STATUS] = status
            joiningDate?.let { preferences[KEY_JOINING_DATE] = it }
            preferences[KEY_IS_LOGGED_IN] = true
            preferences[KEY_REMEMBER_ME] = rememberMe
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun updateToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AUTH_TOKEN] = token
        }
    }

    suspend fun updateProfile(
        firstName: String? = null,
        lastName: String? = null,
        phone: String? = null,
        department: String? = null,
        designation: String? = null
    ) {
        context.dataStore.edit { preferences ->
            firstName?.let { preferences[KEY_FIRST_NAME] = it }
            lastName?.let { preferences[KEY_LAST_NAME] = it }
            phone?.let { preferences[KEY_PHONE] = it }
            department?.let { preferences[KEY_DEPARTMENT] = it }
            designation?.let { preferences[KEY_DESIGNATION] = it }
        }
    }
}
