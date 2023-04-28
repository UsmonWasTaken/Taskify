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

@file:Suppress("NOTHING_TO_INLINE")

package app.taskify.auth.presentation.signup

import android.os.Parcelable
import app.taskify.auth.domain.usecases.signup.SignUpValidationResult
import app.taskify.core.domain.Text
import kotlinx.coroutines.flow.StateFlow
import kotlinx.parcelize.Parcelize

@Parcelize
data class SignUpViewState(
  val displayName: String = "",
  val displayNameError: Text? = null,
  val email: String = "",
  val emailError: Text? = null,
  val password: String = "",
  val passwordError: Text? = null,
  val loadingText: Text? = null,
) : Parcelable {

  inline val isLoading: Boolean
    get() = loadingText != null
}

inline fun StateFlow<SignUpViewState>.viewStateWithUpdatedDisplayName(
  displayName: CharSequence,
): SignUpViewState = value.copy(
  displayName = displayName.toString(),
  displayNameError = null,
)

inline fun StateFlow<SignUpViewState>.viewStateWithUpdatedEmail(
  email: CharSequence,
): SignUpViewState = value.copy(
  email = email.toString(),
  emailError = null,
)

inline fun StateFlow<SignUpViewState>.viewStateWithUpdatedPassword(
  password: CharSequence,
): SignUpViewState = value.copy(
  password = password.toString(),
  passwordError = null,
)

inline fun StateFlow<SignUpViewState>.viewStateWithValidationErrors(
  validationResult: SignUpValidationResult,
): SignUpViewState = value.copy(
  displayNameError = validationResult.displayNameError?.description,
  emailError = validationResult.emailError?.description,
  passwordError = validationResult.passwordError?.description,
)

inline fun StateFlow<SignUpViewState>.viewStateWithLoading(
  loadingText: Text?,
): SignUpViewState = value.copy(loadingText = loadingText)
