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

package app.taskify.profile.presentation.profile

import android.os.Parcelable
import app.taskify.core.domain.Text
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProfileViewState(
  val displayName: String? = null,
  val email: String? = null,
  val loadingText: Text? = null,
) : Parcelable {

  inline val isLoading: Boolean
    get() = loadingText != null
}
