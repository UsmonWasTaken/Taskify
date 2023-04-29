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

package app.taskify.core.domain.preferences

import java.io.Serializable
import kotlinx.coroutines.flow.Flow

interface ProfilePreferences {

  val isAuthenticated: Flow<Boolean>

  val profile: Flow<Profile?>

  suspend fun setProfile(displayName: String?, email: String?, userId: String?)

  suspend fun setProfile(profile: Profile) = setProfile(profile.displayName, profile.email, profile.userId)

  data class Profile(
    val displayName: String?,
    val email: String?,
    val userId: String?,
  ) : Serializable
}
