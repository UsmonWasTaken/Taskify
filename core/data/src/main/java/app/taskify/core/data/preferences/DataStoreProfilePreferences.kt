/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package app.taskify.core.data.preferences

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import app.taskify.core.data.PreferencesDataStore
import app.taskify.core.data.setOrRemove
import app.taskify.core.domain.preferences.ProfilePreferences
import app.taskify.core.domain.preferences.ProfilePreferences.Profile
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreProfilePreferences @Inject constructor(
  private val dataStore: PreferencesDataStore,
) : ProfilePreferences {

  override val profile: Flow<Profile?> = dataStore.data.map { preferences ->
    Profile(preferences[displayNameKey], preferences[emailKey], preferences[userIdKey])
  }

  override val isAuthenticated: Flow<Boolean> = profile.map { it != null }

  override suspend fun setProfile(displayName: String?, email: String?, userId: String?) {
    dataStore.edit { preferences ->
      preferences.setOrRemove(displayNameKey, displayName)
      preferences.setOrRemove(emailKey, email)
      preferences.setOrRemove(userIdKey, userId)
    }
  }

  private companion object {
    val displayNameKey = stringPreferencesKey("display_name_pref_key")
    val emailKey = stringPreferencesKey("email_pref_key")
    val userIdKey = stringPreferencesKey("user_id_pref_key")
  }
}
