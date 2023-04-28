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

import app.taskify.auth.domain.usecases.signup.SignUpValidationResult.DisplayNameError
import app.taskify.auth.domain.usecases.signup.SignUpValidationResult.EmailError
import app.taskify.auth.domain.usecases.signup.SignUpValidationResult.PasswordError
import app.taskify.core.domain.matcher.EmailMatcher
import javax.inject.Inject

class SignUpValidationUseCase @Inject constructor(
  private val emailMatcher: EmailMatcher,
) {

  operator fun invoke(displayName: String, email: String, password: String): SignUpValidationResult {
    val displayNameError = getDisplayNameError(displayName)
    val emailError = getEmailError(email)
    val passwordError = getPasswordError(password)
    return SignUpValidationResult(displayNameError, emailError, passwordError)
  }

  private fun getDisplayNameError(displayName: String): DisplayNameError? = when {
    displayName.isEmpty() -> DisplayNameError.Empty
    displayName.length < MIN_DISPLAY_NAME_LENGTH -> DisplayNameError.TooShort
    displayName.length > MAX_DISPLAY_NAME_LENGTH -> DisplayNameError.TooLong
    else -> null
  }

  private fun getEmailError(email: String): EmailError? = when {
    email.isEmpty() -> EmailError.Empty
    !emailMatcher.matches(email) -> EmailError.Invalid
    else -> null
  }

  private fun getPasswordError(password: String): PasswordError? = when {
    password.isEmpty() -> PasswordError.Empty
    password.length < MIN_PASSWORD_LENGTH -> PasswordError.TooShort
    password.length > MAX_PASSWORD_LENGTH -> PasswordError.TooLong
    else -> null
  }

  companion object {
    const val MIN_DISPLAY_NAME_LENGTH = 2
    const val MAX_DISPLAY_NAME_LENGTH = 32
    const val MIN_PASSWORD_LENGTH = 8
    const val MAX_PASSWORD_LENGTH = 64
  }
}
