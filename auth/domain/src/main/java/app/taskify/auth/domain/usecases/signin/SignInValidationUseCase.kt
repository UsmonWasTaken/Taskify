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

package app.taskify.auth.domain.usecases.signin

import app.taskify.auth.domain.usecases.signin.SignInValidationResult.EmailError
import app.taskify.auth.domain.usecases.signin.SignInValidationResult.PasswordError
import app.taskify.core.domain.matcher.EmailMatcher
import javax.inject.Inject

class SignInValidationUseCase @Inject constructor(
  private val emailMatcher: EmailMatcher,
) {

  operator fun invoke(email: String, password: String): SignInValidationResult {
    val emailError = getEmailError(email)
    val passwordError = getPasswordError(password)
    return SignInValidationResult(emailError, passwordError)
  }

  private fun getEmailError(email: String): EmailError? = when {
    email.isEmpty() -> EmailError.Empty
    !emailMatcher.matches(email) -> EmailError.Invalid
    else -> null
  }

  private fun getPasswordError(password: String): PasswordError? = when {
    password.isEmpty() -> PasswordError.Empty
    else -> null
  }
}
