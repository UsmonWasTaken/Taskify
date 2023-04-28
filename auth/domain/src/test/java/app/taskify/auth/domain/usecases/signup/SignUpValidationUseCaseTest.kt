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
import app.taskify.auth.domain.usecases.signup.SignUpValidationUseCase.Companion.MAX_DISPLAY_NAME_LENGTH
import app.taskify.auth.domain.usecases.signup.SignUpValidationUseCase.Companion.MAX_PASSWORD_LENGTH
import app.taskify.auth.domain.usecases.signup.SignUpValidationUseCase.Companion.MIN_DISPLAY_NAME_LENGTH
import app.taskify.auth.domain.usecases.signup.SignUpValidationUseCase.Companion.MIN_PASSWORD_LENGTH
import app.taskify.core.test.matcher.FakeEmailMatcher
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class SignUpValidationUseCaseTest {

  private lateinit var fakeEmailMatcher: FakeEmailMatcher
  private lateinit var signUpValidationUseCase: SignUpValidationUseCase

  private val defaultDisplayName = "John Doe"
  private val defaultEmail = "hello@johndoe.com"
  private val defaultPassword = "12345678"

  @Before
  fun setup() {
    fakeEmailMatcher = FakeEmailMatcher()
    signUpValidationUseCase = SignUpValidationUseCase(
      emailMatcher = fakeEmailMatcher.mock,
    )
  }

  @Test
  fun emptyDisplayName_returnsSignUpValidationResultWithEmptyDisplayNameError() {
    val displayName = ""
    val email = defaultEmail
    val password = defaultPassword

    fakeEmailMatcher.mockResultForEmail(email, isValidEmail = true)
    val validationResult = signUpValidationUseCase(displayName, email, password)

    assertThat(validationResult.displayNameError).isEqualTo(DisplayNameError.Empty)
  }

  @Test
  fun shortDisplayName_returnsSignUpValidationResultWithShortDisplayNameError() {
    val displayName = "a".repeat(MIN_DISPLAY_NAME_LENGTH - 1)
    val email = defaultEmail
    val password = defaultPassword

    fakeEmailMatcher.mockResultForEmail(email, isValidEmail = true)
    val validationResult = signUpValidationUseCase(displayName, email, password)

    assertThat(validationResult.displayNameError).isEqualTo(DisplayNameError.TooShort)
  }

  @Test
  fun longDisplayName_returnsSignUpValidationResultWithLongDisplayNameError() {
    val displayName = "a".repeat(MAX_DISPLAY_NAME_LENGTH + 1)
    val email = defaultEmail
    val password = defaultPassword

    fakeEmailMatcher.mockResultForEmail(email, isValidEmail = true)
    val validationResult = signUpValidationUseCase(displayName, email, password)

    assertThat(validationResult.displayNameError).isEqualTo(DisplayNameError.TooLong)
  }

  @Test
  fun emptyEmail_returnsSignUpValidationResultWithEmptyEmailError() {
    val displayName = defaultDisplayName
    val email = ""
    val password = defaultPassword

    val validationResult = signUpValidationUseCase(displayName, email, password)

    assertThat(validationResult.emailError).isEqualTo(EmailError.Empty)
    fakeEmailMatcher.verifyEmailMatcherNeverCalled()
  }

  @Test
  fun invalidEmail_returnsSignUpValidationResultWithInvalidEmailError() {
    val displayName = defaultDisplayName
    val email = "hunter2"
    val password = defaultPassword

    fakeEmailMatcher.mockResultForEmail(email, isValidEmail = false)
    val validationResult = signUpValidationUseCase(displayName, email, password)

    assertThat(validationResult.emailError).isEqualTo(EmailError.Invalid)
  }

  @Test
  fun emptyPassword_returnsSignUpValidationResultWithEmptyPasswordError() {
    val displayName = defaultDisplayName
    val email = defaultEmail
    val password = ""

    fakeEmailMatcher.mockResultForEmail(email, isValidEmail = true)
    val validationResult = signUpValidationUseCase(displayName, email, password)

    assertThat(validationResult.passwordError).isEqualTo(PasswordError.Empty)
  }

  @Test
  fun shortPassword_returnsSignUpValidationResultWithShortPasswordError() {
    val displayName = defaultDisplayName
    val email = defaultEmail
    val password = "a".repeat(MIN_PASSWORD_LENGTH - 1)

    fakeEmailMatcher.mockResultForEmail(email, isValidEmail = true)
    val validationResult = signUpValidationUseCase(displayName, email, password)

    assertThat(validationResult.passwordError).isEqualTo(PasswordError.TooShort)
  }

  @Test
  fun longPassword_returnsSignUpValidationResultWithLongPasswordError() {
    val displayName = defaultDisplayName
    val email = defaultEmail
    val password = "a".repeat(MAX_PASSWORD_LENGTH + 1)

    fakeEmailMatcher.mockResultForEmail(email, isValidEmail = true)
    val validationResult = signUpValidationUseCase(displayName, email, password)

    assertThat(validationResult.passwordError).isEqualTo(PasswordError.TooLong)
  }

  @Test
  fun validInputs_returnsNull() {
    val displayName = defaultDisplayName
    val email = defaultEmail
    val password = defaultPassword

    fakeEmailMatcher.mockResultForEmail(email, isValidEmail = true)
    val validationResult = signUpValidationUseCase(displayName, email, password)

    assertThat(validationResult.areInputsValid).isTrue()
  }
}
