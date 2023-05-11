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
import app.taskify.auth.domain.usecases.signup.SignUpValidationUseCase.Companion.MAX_DISPLAY_NAME_LENGTH
import app.taskify.auth.domain.usecases.signup.SignUpValidationUseCase.Companion.MAX_PASSWORD_LENGTH
import app.taskify.auth.domain.usecases.signup.SignUpValidationUseCase.Companion.MIN_DISPLAY_NAME_LENGTH
import app.taskify.auth.domain.usecases.signup.SignUpValidationUseCase.Companion.MIN_PASSWORD_LENGTH
import app.taskify.core.domain.Text

data class SignUpValidationResult(
  val displayNameError: DisplayNameError? = null,
  val emailError: EmailError? = null,
  val passwordError: PasswordError? = null,
) {

  inline val areInputsValid: Boolean
    get() = displayNameError == null && emailError == null && passwordError == null

  sealed class DisplayNameError(val description: Text) {
    object Empty : DisplayNameError(Text(R.string.empty_display_name))
    object TooShort : DisplayNameError(Text(R.string.short_display_name, MIN_DISPLAY_NAME_LENGTH))
    object TooLong : DisplayNameError(Text(R.string.long_display_name, MAX_DISPLAY_NAME_LENGTH))
  }

  sealed class EmailError(val description: Text) {
    object Empty : EmailError(Text(R.string.empty_email))
    object Invalid : EmailError(Text(R.string.invalid_email))
  }

  sealed class PasswordError(val description: Text) {
    object Empty : PasswordError(Text(R.string.empty_password))
    object TooShort : PasswordError(Text(R.string.short_password, MIN_PASSWORD_LENGTH))
    object TooLong : PasswordError(Text(R.string.short_password, MAX_PASSWORD_LENGTH))
  }
}
