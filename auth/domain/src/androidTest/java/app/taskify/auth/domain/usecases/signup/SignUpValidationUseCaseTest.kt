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

import app.taskify.auth.domain.R.string
import app.taskify.auth.domain.usecases.signup.SignUpValidationUseCase.Companion.MAX_DISPLAY_NAME_LENGTH
import app.taskify.auth.domain.usecases.signup.SignUpValidationUseCase.Companion.MAX_PASSWORD_LENGTH
import app.taskify.auth.domain.usecases.signup.SignUpValidationUseCase.Companion.MIN_DISPLAY_NAME_LENGTH
import app.taskify.auth.domain.usecases.signup.SignUpValidationUseCase.Companion.MIN_PASSWORD_LENGTH
import app.taskify.core.domain.Text
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

    assertThat(validationResult).isNotNull()
    assertThat(validationResult?.displayNameError).isEqualTo(Text(string.empty_display_name))
  }

  @Test
  fun shortDisplayName_returnsSignUpValidationResultWithShortDisplayNameError() {
    val displayName = "a".repeat(MIN_DISPLAY_NAME_LENGTH - 1)
    val email = defaultEmail
    val password = defaultPassword

    fakeEmailMatcher.mockResultForEmail(email, isValidEmail = true)
    val validationResult = signUpValidationUseCase(displayName, email, password)

    assertThat(validationResult).isNotNull()
    assertThat(validationResult?.displayNameError).isEqualTo(Text(string.short_display_name, MIN_DISPLAY_NAME_LENGTH))
  }

  @Test
  fun longDisplayName_returnsSignUpValidationResultWithLongDisplayNameError() {
    val displayName = "a".repeat(MAX_DISPLAY_NAME_LENGTH + 1)
    val email = defaultEmail
    val password = defaultPassword

    fakeEmailMatcher.mockResultForEmail(email, isValidEmail = true)
    val validationResult = signUpValidationUseCase(displayName, email, password)

    assertThat(validationResult).isNotNull()
    assertThat(validationResult?.displayNameError).isEqualTo(Text(string.long_display_name, MAX_DISPLAY_NAME_LENGTH))
  }

  @Test
  fun emptyEmail_returnsSignUpValidationResultWithEmptyEmailError() {
    val displayName = defaultDisplayName
    val email = ""
    val password = defaultPassword

    val validationResult = signUpValidationUseCase(displayName, email, password)

    assertThat(validationResult).isNotNull()
    assertThat(validationResult?.emailError).isEqualTo(Text(string.empty_email))
    fakeEmailMatcher.verifyEmailMatcherNeverCalled()
  }

  @Test
  fun invalidEmail_returnsSignUpValidationResultWithInvalidEmailError() {
    val displayName = defaultDisplayName
    val email = "hunter2"
    val password = defaultPassword

    fakeEmailMatcher.mockResultForEmail(email, isValidEmail = false)
    val validationResult = signUpValidationUseCase(displayName, email, password)

    assertThat(validationResult).isNotNull()
    assertThat(validationResult?.emailError).isEqualTo(Text(string.invalid_email))
  }

  @Test
  fun emptyPassword_returnsSignUpValidationResultWithEmptyPasswordError() {
    val displayName = defaultDisplayName
    val email = defaultEmail
    val password = ""

    fakeEmailMatcher.mockResultForEmail(email, isValidEmail = true)
    val validationResult = signUpValidationUseCase(displayName, email, password)
    assertThat(validationResult).isNotNull()
    assertThat(validationResult?.passwordError).isEqualTo(Text(string.empty_password))
  }

  @Test
  fun shortPassword_returnsSignUpValidationResultWithShortPasswordError() {
    val displayName = defaultDisplayName
    val email = defaultEmail
    val password = "a".repeat(MIN_PASSWORD_LENGTH - 1)

    fakeEmailMatcher.mockResultForEmail(email, isValidEmail = true)
    val validationResult = signUpValidationUseCase(displayName, email, password)
    assertThat(validationResult).isNotNull()
    assertThat(validationResult?.passwordError).isEqualTo(Text(string.short_password, MIN_PASSWORD_LENGTH))
  }

  @Test
  fun longPassword_returnsSignUpValidationResultWithLongPasswordError() {
    val displayName = defaultDisplayName
    val email = defaultEmail
    val password = "a".repeat(MAX_PASSWORD_LENGTH + 1)

    fakeEmailMatcher.mockResultForEmail(email, isValidEmail = true)
    val validationResult = signUpValidationUseCase(displayName, email, password)
    assertThat(validationResult).isNotNull()
    assertThat(validationResult?.passwordError).isEqualTo(Text(string.long_password, MAX_PASSWORD_LENGTH))
  }

  @Test
  fun validInputs_returnsNull() {
    val displayName = defaultDisplayName
    val email = defaultEmail
    val password = defaultPassword

    fakeEmailMatcher.mockResultForEmail(email, isValidEmail = true)
    val validationResult = signUpValidationUseCase(displayName, email, password)

    assertThat(validationResult).isNull()
  }
}
