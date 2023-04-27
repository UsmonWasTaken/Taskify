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

package app.taskify.auth.domain.usecases.signup

import app.taskify.auth.domain.R
import app.taskify.core.domain.Text
import app.taskify.core.domain.matcher.EmailMatcher
import javax.inject.Inject

class SignUpValidationUseCase @Inject constructor(
  private val emailMatcher: EmailMatcher,
) {

  operator fun invoke(displayName: String, email: String, password: String): SignUpValidationResult? {
    val displayNameError = getDisplayNameError(displayName)
    val emailError = getEmailError(email)
    val passwordError = getPasswordError(password)
    return SignUpValidationResult(displayNameError, emailError, passwordError).takeIf {
      displayNameError != null || emailError != null || passwordError != null
    }
  }

  private fun getDisplayNameError(displayName: String): Text? = when {
    displayName.isEmpty() -> Text(R.string.empty_display_name)
    displayName.length < MIN_DISPLAY_NAME_LENGTH -> Text(R.string.short_display_name, MIN_DISPLAY_NAME_LENGTH)
    displayName.length > MAX_DISPLAY_NAME_LENGTH -> Text(R.string.long_display_name, MAX_DISPLAY_NAME_LENGTH)
    else -> null
  }

  private fun getEmailError(email: String): Text? = when {
    email.isEmpty() -> Text(R.string.empty_email)
    !emailMatcher.matches(email) -> Text(R.string.invalid_email)
    else -> null
  }

  private fun getPasswordError(password: String): Text? = when {
    password.isEmpty() -> Text(R.string.empty_password)
    password.length < MIN_PASSWORD_LENGTH -> Text(R.string.short_password, MIN_PASSWORD_LENGTH)
    password.length > MAX_PASSWORD_LENGTH -> Text(R.string.long_password, MAX_PASSWORD_LENGTH)
    else -> null
  }

  internal companion object {
    const val MIN_DISPLAY_NAME_LENGTH = 2
    const val MAX_DISPLAY_NAME_LENGTH = 32
    const val MIN_PASSWORD_LENGTH = 8
    const val MAX_PASSWORD_LENGTH = 64
  }
}
