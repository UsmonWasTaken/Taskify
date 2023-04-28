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

import app.taskify.auth.domain.R
import app.taskify.core.domain.Text

data class SignInValidationResult(
  val emailError: EmailError?,
  val passwordError: PasswordError?,
) {

  inline val areInputsValid: Boolean
    get() = emailError == null && passwordError == null

  sealed class EmailError(val description: Text) {
    object Empty : EmailError(Text(R.string.empty_email))
    object Invalid : EmailError(Text(R.string.invalid_email))
  }

  sealed class PasswordError(val description: Text) {
    object Empty : PasswordError(Text(R.string.empty_password))
  }
}
