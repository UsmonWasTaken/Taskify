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

package app.taskify.auth.presentation.signin

import android.os.Parcelable
import app.taskify.auth.domain.usecases.signin.SignInValidationResult
import app.taskify.core.domain.Text
import kotlinx.coroutines.flow.StateFlow
import kotlinx.parcelize.Parcelize

@Parcelize
data class SignInViewState(
  val email: String = "",
  val emailError: Text? = null,
  val password: String = "",
  val passwordError: Text? = null,
  val loadingText: Text? = null,
) : Parcelable {

  inline val isLoading: Boolean
    get() = loadingText != null
}

inline fun StateFlow<SignInViewState>.viewStateWithUpdatedEmail(
  email: CharSequence,
): SignInViewState = value.copy(
  email = email.toString(),
  emailError = null,
)

inline fun StateFlow<SignInViewState>.viewStateWithUpdatedPassword(
  password: CharSequence,
): SignInViewState = value.copy(
  password = password.toString(),
  passwordError = null,
)

inline fun StateFlow<SignInViewState>.viewStateWithValidationErrors(
  validationResult: SignInValidationResult,
): SignInViewState = value.copy(
  emailError = validationResult.emailError?.description,
  passwordError = validationResult.passwordError?.description,
)

inline fun StateFlow<SignInViewState>.viewStateWithLoading(
  loadingText: Text?,
): SignInViewState = value.copy(loadingText = loadingText)
